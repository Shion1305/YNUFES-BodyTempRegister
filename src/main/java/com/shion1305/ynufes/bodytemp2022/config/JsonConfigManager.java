/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


public class JsonConfigManager {
    static Logger logger = Logger.getLogger("JsonConfigManager");


    public static InstanceData[] readJson() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonPath = ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON);
        if (jsonPath == null) {
            logger.severe("Configuration Json not specified");
            return null;
        }
        InstanceData[] data;
        try {
            if ((data = mapper.readValue(new File(jsonPath), InstanceData[].class)) == null) {
                logger.info("JsonConfig loaded, but no profile found.");
            }
        } catch (IOException e) {
            logger.warning("JsonConfig\"" + ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON) + "\" not found");
            return null;
        }
        return data;
    }
}
