/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.shion1305.ynufes.bodytemp2022.config.InstanceData;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RequestProcessor {
    private final Logger logger;
    private final Preferences preferences;
    private final GASConnector connector;
    private final InstanceData data;
    private final LineMessageSender sender;

    public RequestProcessor(InstanceData data) {
        this.logger = Logger.getLogger("RequestProcessor{" + data.processName + "}");
        this.data = data;
        connector = new GASConnector(data.gasUrl);
        preferences = Preferences.userRoot().node("ynufes-bodytemp").node(data.processName);
        sender = new LineMessageSender(data.processName, data.lineToken);
    }

    public void processRequest(String in, String userId, String token) throws BackingStoreException, IOException {
        logger.info(String.format("[%s]Received message from %s, content: %s", data.processName, userId, in));
        String name = preferences.get(userId, null);
        if (name == null) {
            registerName(in, userId, token);
        } else {
            registerTemp(in, name, token, userId);
        }
    }

    private void registerName(String in, String userId, String token) throws IOException, BackingStoreException {
        //avoid injection
        if (in.contains("&") || in.length() > 10)
            sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token);
        logger.info(String.format("[%s]%s tried to register name %s", data.processName, userId, in));
        if (connector.checkName(in)) {
            preferences.put(userId, in);
            preferences.flush();
            logger.info(String.format("[%s]%s registered its name %s", data.processName, userId, in));
            sender.nameSuccess(in, token);
        } else {
            sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token);
        }
    }

    private void registerTemp(String temp, String name, String token, String userId) throws IOException, BackingStoreException {
        if (!temp.matches("^[34]\\d\\.\\d$")) {
            sender.sendError(LineMessageSender.ErrorType.INVALID_TEMP_FORMAT, token);
            return;
        }
        int result = connector.register(name, temp);
        logger.info(String.format("[%s]%s(%s) requested to register temp, result was %d", data.processName, name, userId, result));
        switch (result) {
            case 202:
                sender.tempSuccess(temp, name, token);
                break;
            case 404:
                sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token);
                preferences.remove(userId);
                preferences.flush();
                break;
            case 500:
                sender.sendError(LineMessageSender.ErrorType.SERVER_ERROR, token);
                break;
            default:
                sender.sendError(LineMessageSender.ErrorType.ERROR_UNKNOWN, token);
        }
    }

    public void checkNoSubmission() throws BackingStoreException, IOException {
        if (!data.enabled) return;
        logger.info(String.format("[%s]Checking submission status...", data.processName));

//        Arrays.stream(preferences.keys()).parallel().forEach(userID -> {
//            try {
//                String name = preferences.get(userID, null);
//                if (name == null) return;
//                String result = connector.checkRecord(name);
//                if (result == null || !result.equals("")) return;
//                logger.info(String.format("Sending Late reminder to %s(%s)", name, userID));
//                sender.sendLateReminder(userID);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
        for (String userID : preferences.keys()) {
            String name = preferences.get(userID, null);
            if (name == null) continue;
            String result = connector.checkRecord(name);
            if (result == null || !result.equals("")) continue;
            logger.info(String.format("[%s]Sending Late reminder to %s(%s)", data.processName, name, userID));
            sender.sendLateReminder(userID);
        }
        logger.info(String.format("[%s]All submission status checks are finished.", data.processName));
    }

    public void processEvent(Event e) throws BackingStoreException, IOException {
        if (e instanceof UnfollowEvent) {
            //UnfollowedEventで登録を削除。
            logger.info(String.format("[%s]User %s unfollowed the bot", data.processName, e.getSource().getUserId()));
            preferences.remove(e.getSource().getUserId());
            preferences.flush();
            return;
        }
        if (data.enabled) {
            if (e instanceof FollowEvent) {
                logger.info(String.format("[%s]User %s started following the bot", data.processName, e.getSource().getUserId()));
                sender.sendWelcomeMessage(((FollowEvent) e).getReplyToken());
            } else if (e instanceof MessageEvent) {
                String token = ((MessageEvent<?>) e).getReplyToken();
                MessageContent mes = ((MessageEvent<?>) e).getMessage();
                if (mes instanceof TextMessageContent) {
                    String message = ((TextMessageContent) mes).getText();
                    processRequest(message, e.getSource().getUserId(), token);
                } else {
                    sender.sendError(LineMessageSender.ErrorType.InvalidFormat, token);
                }
            }
        } else {
            if (e instanceof FollowEvent) {
                sender.notifyDisabled(((FollowEvent) e).getReplyToken());
            } else if (e instanceof MessageEvent) {
                sender.notifyDisabled(((MessageEvent<?>) e).getReplyToken());
            }
        }
    }

    public void broadcastReminder() {
        if (!data.enabled) return;
        sender.broadcastReminder();
    }
}
