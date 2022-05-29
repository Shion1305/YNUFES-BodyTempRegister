package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LineMessageSender {
    private static LineMessagingClient client;

    static {
        client = LineMessagingClient.builder(ConfigManager.getConfig("BotToken")).build();
    }

    public static void broadcast(Message message){
        try {
            client.broadcast(new Broadcast(message)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    public static void reply(ReplyMessage message) {
        try {
            BotApiResponse response = client.replyMessage(message).get(30, TimeUnit.SECONDS);
            System.out.println("REPLIED");
            if (response == null) {
                System.out.println("NULL");
            } else System.out.println(response.toString());
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }
}
