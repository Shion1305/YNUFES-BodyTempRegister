package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.parser.SignatureValidator;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;
import com.shion1305.ynufes.bodytemp2022.message.MessageSender;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;

@WebServlet("/line")
public class LineEndpointReceiver extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebhookParser parser = new WebhookParser(new SignatureValidator() {
            @Override
            public boolean validateSignature(byte[] content, String headerSignature) {
                return true;
            }
        });
        try {
            CallbackRequest request = parser.handle("teset", req.getInputStream().readAllBytes());
            List<Event> events = request.getEvents();
            for (var event : events) {
                processEvent(event);
            }
        } catch (WebhookParseException | BackingStoreException e) {
            throw new RuntimeException(e);
        }
        resp.setStatus(200);
    }

    public void processEvent(Event e) throws BackingStoreException, IOException {
        if (e instanceof FollowEvent) {
            MessageSender.sendWelcomeMessage(((FollowEvent) e).getReplyToken());
        } else if (e instanceof MessageEvent) {
            String token = ((MessageEvent<?>) e).getReplyToken();
            MessageContent mes = ((MessageEvent<?>) e).getMessage();
            if (mes instanceof TextMessageContent) {
                String message = ((TextMessageContent) mes).getText();
                UserHandler.processRequest(message, e.getSource().getUserId(), token);
            } else {
                MessageSender.sendError(MessageSender.ErrorType.InvalidFormat, token);
            }
        }
    }
}
