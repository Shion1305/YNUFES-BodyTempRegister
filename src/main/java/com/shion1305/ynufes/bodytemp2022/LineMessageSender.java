package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class LineMessageSender {
    private static final Logger logger = Logger.getLogger("MessageSender");
    private static LineMessagingClient client;

    static {
        client = LineMessagingClient.builder(ConfigManager.getConfig("BotToken")).build();
    }

    public static void broadcast(Message message) {
        try {
            client.broadcast(new Broadcast(message)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reply(ReplyMessage message) {
        try {
            client.replyMessage(message).get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            logger.severe("ERROR in sending Message");
            e.printStackTrace();
        }
    }

    public static void push(PushMessage message) {
        client.pushMessage(message);
    }
}
