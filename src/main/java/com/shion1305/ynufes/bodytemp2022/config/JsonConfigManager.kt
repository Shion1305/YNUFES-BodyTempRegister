/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.config

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.util.logging.Logger

object JsonConfigManager {
    var logger = Logger.getLogger("JsonConfigManager")
    fun readJson(): Array<InstanceData>? {
        val mapper = ObjectMapper()
        val jsonPath = ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON)
        if (jsonPath == null) {
            logger.severe("Configuration Json not specified")
            return null
        }
        var data: Array<InstanceData>?
        try {
            if (mapper.readValue(File(jsonPath), Array<InstanceData>::class.java).also { data = it } == null) {
                logger.info("JsonConfig loaded, but no profile found.")
            }
        } catch (e: IOException) {
            logger.warning("JsonConfig\"" + ConfigManager.getConfig(ConfigManager.ConfigProperty.BOT_CONFIG_JSON) + "\" not found")
            return null
        }
        return data
    }
}