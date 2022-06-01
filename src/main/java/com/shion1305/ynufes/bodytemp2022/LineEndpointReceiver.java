package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.parser.SignatureValidator;
import com.linecorp.bot.parser.WebhookParseException;
import com.linecorp.bot.parser.WebhookParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

@WebServlet("/line/*")
public class LineEndpointReceiver extends HttpServlet {
    private static final Logger logger = Logger.getLogger("LineEndpointReceiver");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getServletPath();
        String reqName = url.substring(url.indexOf('/') + 1);
        WebhookParser parser = new WebhookParser((content, headerSignature) -> true);
        try {
            CallbackRequest request = parser.handle("teset", req.getInputStream().readAllBytes());
            List<Event> events = request.getEvents();
            for (var event : events) {
                ProcessorManager.processEvent(reqName, event);
            }
        } catch (WebhookParseException | BackingStoreException e) {
            throw new RuntimeException(e);
        }
        resp.setStatus(200);
    }
}
