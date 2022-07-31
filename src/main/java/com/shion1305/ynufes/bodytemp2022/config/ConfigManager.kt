/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.config

import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.logging.Logger

object ConfigManager {
    private val logger = Logger.getLogger("ConfigManager")
    private var config: Properties? = null
    private val configDir = System.getProperty("user.home") + "/ShionServerConfig/YNUFES-BodyTempBot/config.properties"

    init {
        init()
    }

    private fun init() {
        config = Properties()
        try {
            FileInputStream(configDir).use { s ->
                logger.info("Configuration is Loaded")
                config!!.load(s)
            }
        } catch (e: IOException) {
            logger.severe("Configuration LOAD FAILED")
            e.printStackTrace()
        }
    }

    fun getConfig(field: String?): String {
        return config!!.getProperty(field)
    }

    fun getConfig(property: ConfigProperty?): String? {
        return when (property) {
            ConfigProperty.BOT_CONFIG_JSON -> getConfig(
                "BotConfigJson"
            )

            ConfigProperty.MAINTENANCE_PASS -> getConfig(
                "MaintenancePass"
            )

            else -> null
        }
    }

    fun refresh() {
        init()
    }

    enum class ConfigProperty {
        BOT_CONFIG_JSON, MAINTENANCE_PASS
    }
}