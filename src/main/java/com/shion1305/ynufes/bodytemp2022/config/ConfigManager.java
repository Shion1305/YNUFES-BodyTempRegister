/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Logger logger = Logger.getLogger("ConfigManager");
    private static Properties config;

    public enum ConfigProperty {
        BOT_CONFIG_JSON
    }

    private final static String configDir = System.getProperty("user.home") + "/ShionServerConfig/YNUFES-BodyTempBot/config.properties";

    static {
        init();
    }

    private static void init() {
        config = new Properties();
        try (FileInputStream s = new FileInputStream(configDir)) {
            logger.info("Configuration is Loaded");
            config.load(s);
        } catch (IOException e) {
            logger.severe("Configuration LOAD FAILED");
            e.printStackTrace();
        }
    }

    public static String getConfig(String field) {
        return config.getProperty(field);
    }

    public static String getConfig(ConfigProperty property) {
        switch (property) {
            case BOT_CONFIG_JSON:
                return getConfig("BotConfigJson");
            default:
                return null;
        }
    }

    public static void refresh() {
        init();
    }
}
