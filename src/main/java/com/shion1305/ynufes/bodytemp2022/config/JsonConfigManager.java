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


    public static InstanceData[] readJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InstanceData[] data = mapper.readValue(new File(ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON)), InstanceData[].class);
        if (data == null) {
            logger.info("JsonConfig\"" + ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON) + "\" is not loaded properly");
        } else {
            logger.info("JsonConfigFound");
        }
        return data;
    }
}
