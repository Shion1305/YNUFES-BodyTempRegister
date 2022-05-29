package com.shion1305.ynufes.bodytemp2022;

import com.shion1305.ynufes.bodytemp2022.message.MessageSender;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserHandler {
    private static final Logger logger = Logger.getLogger("UserHandler");
    private static final Preferences preferences;
    private final static String urlString = ConfigManager.getConfig("GASUrl");
    static URL url;
    static GASConnector connector = new GASConnector(urlString);

    static {
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        preferences = Preferences.userRoot().node("ynufes-hensyu-bodytemp").node("hensyu");
    }

    public static void processRequest(String in, String userId, String token) throws BackingStoreException, IOException {
        String name = preferences.get(userId, null);
        if (name == null) {
            registerName(in, userId, token);
        } else {
            registerTemp(in, name, token, userId);
        }
    }

    private static void registerName(String in, String userId, String token) throws IOException, BackingStoreException {
        //avoid injection
        if (in.contains("&") || in.length() > 10)
            MessageSender.sendError(MessageSender.ErrorType.NAME_NOT_FOUND, token);
        if (connector.check(in)) {
            preferences.put(userId, in);
            preferences.flush();
            MessageSender.nameSuccess(in, token);
        } else {
            MessageSender.sendError(MessageSender.ErrorType.NAME_NOT_FOUND, token);
        }
    }

    private static void registerTemp(String temp, String name, String token, String userId) throws IOException {
        if (!temp.matches("^[34]\\d\\.\\d$")) {
            MessageSender.sendError(MessageSender.ErrorType.INVALID_TEMP_FORMAT, token);
            return;
        }
        int result = connector.register(name, temp);
        logger.info(String.format("%s(%s) requested to register temp, result was %d", name, userId, result));
        switch (result) {
            case 202:
                MessageSender.tempSuccess(temp, name, token);
                break;
            case 404:
                MessageSender.sendError(MessageSender.ErrorType.NAME_NOT_FOUND, token);
                preferences.remove(userId);
                break;
            case 500:
                MessageSender.sendError(MessageSender.ErrorType.SERVER_ERROR, token);
                break;
            default:
                MessageSender.sendError(MessageSender.ErrorType.ERROR_UNKNOWN, token);
        }
    }
}
