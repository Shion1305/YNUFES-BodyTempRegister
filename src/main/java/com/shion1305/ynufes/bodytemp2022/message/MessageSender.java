package com.shion1305.ynufes.bodytemp2022.message;

import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.shion1305.ynufes.bodytemp2022.LineMessageSender;

public class ErrorMessageSender {
    public enum Type {InvalidFormat, InvalidCheck, InvalidTemp, NAME_NOT_FOUND}

    public static void sendError(Type t, String replyToken) {
        String mes = "";
        switch (t) {
            case InvalidFormat:
                mes = "不正な形式のメッセージです。テキストメッセージのみ有効です。";
        }
        Text title = Text.builder().text("エラー").build();
        Text content = Text.builder().text(mes).build();
        Box box = Box.builder().contents(title, content).build();
        Bubble b = Bubble.builder().body(box).build();
        FlexMessage message = FlexMessage.builder().contents(b).build();
        LineMessageSender.reply(new ReplyMessage(replyToken, message));
    }
}
