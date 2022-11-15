/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.line;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class LineMessageSender {
    private static final Logger logger = Logger.getLogger("MessageSender");
    private static final Random random = new Random();
    private final String processName;
    private final LineMessagingClient client;

    public LineMessageSender(String processName, String botToken) {
        this.processName = processName;
        this.client = LineMessagingClient.builder(botToken).build();
    }

    private void broadcast(Message message) {
        try {
            client.broadcast(new Broadcast(message)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void reply(String replyToken, Message message) {
        try {
            client.replyMessage(new ReplyMessage(replyToken, message)).get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            logger.severe(String.format("[%s]ERROR in sending Message", processName));
            e.printStackTrace();
        }
    }

    private void push(PushMessage message) {
        client.pushMessage(message);
    }

    public void sendThankYou(String token) {
        // send Line stamp
        Message message;
        switch (random.nextInt(5)) {
            case 0:
                message = new StickerMessage("11539", "52114110");
                break;
            case 1:
                message = new StickerMessage("11538", "51626494");
                break;
            case 2:
                message = new StickerMessage("11537", "52002735");
                break;
            case 3:
                message = new StickerMessage("8515", "16581243");
                break;
            case 4:
                message = new StickerMessage("6136", "10551378");
                break;
            default:
                message = new StickerMessage("11539", "52114110");
        }
        reply(token, message);
    }

    public enum ErrorType {InvalidFormat, NAME_NOT_FOUND, INVALID_TEMP_FORMAT, SERVER_ERROR, ERROR_UNKNOWN}

    public void sendWelcomeMessage(String replyToken) {
        FlexMessage message = standardMessage("体温入力BOTへようこそ", "まずはじめに\n名前をスペースを入れずに入力してください(例:市川詩恩)", "体温入力BOTへようこそ");
        reply(replyToken, message);
    }

    public void nameSuccess(String name, String token) {
        Text title = createLabel("名前を登録しました", true);
        Text description = createLabel("以下の内容で登録しました", false);
        Map<String, String> data = new HashMap<>();
        data.put("名前", name);
        Box mainBox = Box.builder().layout(FlexLayout.VERTICAL)
                .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build();
        reply(token, FlexMessage.builder()
                .contents(Bubble.builder().body(mainBox).build())
                .altText("名前登録完了").build());
    }

    public void tempSuccess(String temp, String name, String replyToken) {
        Text title = createLabel("体温を登録しました", true);
        Text description = createLabel("以下の内容で登録しました", false);
        Map<String, String> data = new HashMap<>();
        data.put("名前", name);
        data.put("体温", temp);
        data.put("日付", new SimpleDateFormat("MM/dd").format(new Date()));
        Box mainBox = Box.builder().layout(FlexLayout.VERTICAL)
                .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build();
        Bubble bubble = Bubble.builder().body(mainBox).build();
        FlexMessage message = FlexMessage.builder().contents(bubble).altText("体温登録成功").build();
        reply(replyToken, message);
    }

    private Box listItems(Map<String, String> data) {
        List<FlexComponent> contents = new ArrayList<>();
        for (Map.Entry<String, String> d : data.entrySet()) {
            contents.add(Box.builder().layout(FlexLayout.BASELINE)
                    .spacing(FlexMarginSize.SM)
                    .contents(Text.builder().text(d.getKey()).flex(1).size(FlexFontSize.SM).color("#aaaaaa").build(),
                            Text.builder().text(d.getValue()).flex(4).size(FlexFontSize.SM).color("#666666").build())
                    .build());
        }
        return Box.builder().layout(FlexLayout.VERTICAL)
                .contents(contents).spacing(FlexMarginSize.SM).build();
    }

    public void sendError(ErrorType t, String replyToken) {
        String mes;
        switch (t) {
            case InvalidFormat:
                mes = "不正な形式のメッセージです。テキストメッセージのみ有効です。";
                break;
            case INVALID_TEMP_FORMAT:
                mes = "体温の形式が正しくありません。正しく入力してください(例: 36.0)";
                break;
            case SERVER_ERROR:
                mes = "SpreadSheet側でエラーが発生しました。\nSpreadSheet側で今日の欄が登録されていない可能性があります。編集部市川までご連絡ください。";
                break;
            case NAME_NOT_FOUND:
                mes = "SpreadSheet上で入力された名前は見つかりませんでした。スペースを入れずに名前を入力してください(例:市川詩恩)。解決しない場合は編集部市川まで";
                break;
            case ERROR_UNKNOWN:
            default:
                mes = "不明のエラーが発生しました。";
        }
        logger.info(String.format("[%s]ERROR, %s", processName, t.name()));
        Text title = createLabel("エラー", true);
        Text content = createLabel(mes, false);
        Box box = Box.builder().contents(title, content).layout(FlexLayout.VERTICAL).build();
        Bubble b = Bubble.builder().body(box).build();
        FlexMessage message = FlexMessage.builder().contents(b).altText("エラーメッセージ").build();
        reply(replyToken, message);
    }

    static Text createLabel(String s, boolean bold) {
        return Text.builder().text(s).weight(bold ? Text.TextWeight.BOLD : Text.TextWeight.REGULAR).wrap(true).build();
    }

    public void broadcastReminder() {
        FlexMessage message = standardMessage("検温忘れずに!", "検温しましょう!このチャットに体温を送信してください!(例:36.4)", "検温リマインダー");
        broadcast(message);
        logger.info(String.format("[%s]Reminder broadcasted", processName));
    }

    public FlexMessage standardMessage(String title, String content, String altText) {
        Box b = Box.builder()
                .layout(FlexLayout.VERTICAL)
                .contents(createLabel(title, true), createLabel(content, false)).build();
        Bubble bubble = Bubble.builder().body(b).build();
        return FlexMessage.builder()
                .contents(bubble)
                .altText(altText)
                .build();
    }

    public void sendLateReminder(String userID) {
        FlexMessage message = standardMessage("入力を忘れていませんか?", "検温入力が御済みでないようです!忘れずに入力しましょう!", "検温入力忘れていませんか?");
        push(new PushMessage(userID, message));
    }

    public void warnDisabled(String replyToken) {
        FlexMessage message = standardMessage("稼働停止中", "このBOTは現在稼働停止中です。\n詳細は編集部市川まで", "稼働停止中");
        reply(replyToken, message);
    }

    public void notifyDisabled() {
        FlexMessage message = standardMessage("稼働停止しました", "このBOTは無効化されました。\n詳細は編集部市川まで", "稼働停止通知");
        broadcast(message);
    }

    public void notifyEnabled() {
        FlexMessage message = standardMessage("稼働再開しました", "このBOTは稼働を再開しました。", "稼働再開通知");
        broadcast(message);
    }

    public long getUsage() {
        try {
            return client.getMessageQuotaConsumption().get().getTotalUsage();
        } catch (InterruptedException | ExecutionException e) {
            logger.warning("Failed to get usage data");
            return -1;
        }
    }

    public void requestNumFollowers(ProcessorManager.StatusData data) {
        client.getNumberOfFollowers(new SimpleDateFormat("yyyyMMdd").format(Instant.now()
                        .toEpochMilli()))
                .whenComplete((num, ex) -> {
                    if (ex != null) {
                        logger.warning("Failed to get number of followers");
                        data.numFollowers.status = ProcessorManager.StatusData.LineNumInfo.Status.ERROR;
                    } else {
                        if (num.getStatus().name().equals("READY")) {
                            data.numFollowers.status = ProcessorManager.StatusData.LineNumInfo.Status.READY;
                            data.numFollowers.followers = num.getFollowers();
                            data.numFollowers.blockers = num.getBlocks();
                            data.numFollowers.targetReaches = num.getTargetedReaches();
                        } else {
                            data.numFollowers.status = ProcessorManager.StatusData.LineNumInfo.Status.NOT_READY;
                        }
                    }
                });
    }
}
