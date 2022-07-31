/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.contoller

import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.FollowEvent
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.UnfollowEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.shion1305.ynufes.bodytemp2022.config.InstanceData
import com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager.StatusData
import com.shion1305.ynufes.bodytemp2022.gas.GASConnector
import com.shion1305.ynufes.bodytemp2022.gas.GASManager
import com.shion1305.ynufes.bodytemp2022.line.LineMessageSender
import java.io.IOException
import java.util.*
import java.util.logging.Logger
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences

class RequestProcessor(data: InstanceData) {
    private val logger: Logger
    private val preferences: Preferences
    private var connector: GASConnector? = null

    @Volatile
    private var data: InstanceData? = null
    private val sender: LineMessageSender

    /**
     * InstanceDataのうち以下はfinalとして扱う(変更不可)
     * processName
     * lineToken
     */
    init {
        logger = Logger.getLogger("RequestProcessor{" + data.processName + "}")
        preferences = Preferences.userRoot().node("ynufes-bodytemp").node(data.processName)
        sender = LineMessageSender(data.processName, data.lineToken)
        init(data)
    }

    private fun init(data: InstanceData) {
        this.data = data
        connector = GASManager.getGASConnector(data.gasUrl)
    }

    @Throws(BackingStoreException::class, IOException::class)
    fun processRequest(`in`: String, userId: String?, token: String) {
        logger.info(String.format("[%s]Received message from %s, content: %s", data!!.processName, userId, `in`))
        val name = preferences[userId, null]
        if (name == null) {
            registerName(`in`, userId, token)
        } else {
            registerTemp(`in`, name, token, userId)
        }
    }

    @Throws(IOException::class, BackingStoreException::class)
    private fun registerName(`in`: String, userId: String?, token: String) {
        //avoid injection
        if (`in`.contains("&") || `in`.length > 10) sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token)
        logger.info(String.format("[%s]%s tried to register name %s", data!!.processName, userId, `in`))
        if (connector!!.checkName(`in`)) {
            preferences.put(userId, `in`)
            preferences.flush()
            logger.info(String.format("[%s]%s registered its name %s", data!!.processName, userId, `in`))
            sender.nameSuccess(`in`, token)
        } else {
            sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token)
        }
    }

    @Throws(IOException::class, BackingStoreException::class)
    private fun registerTemp(temp: String, name: String, token: String, userId: String?) {
        if (!temp.matches("^[34]\\d\\.\\d$")) {
            sender.sendError(LineMessageSender.ErrorType.INVALID_TEMP_FORMAT, token)
            return
        }
        val result = connector!!.register(name, temp)
        logger.info(
            String.format(
                "[%s]%s(%s) requested to register temp, result was %d",
                data!!.processName,
                name,
                userId,
                result
            )
        )
        when (result) {
            202 -> sender.tempSuccess(temp, name, token)
            404 -> {
                sender.sendError(LineMessageSender.ErrorType.NAME_NOT_FOUND, token)
                preferences.remove(userId)
                preferences.flush()
            }

            500 -> sender.sendError(LineMessageSender.ErrorType.SERVER_ERROR, token)
            else -> sender.sendError(LineMessageSender.ErrorType.ERROR_UNKNOWN, token)
        }
    }

    fun isReloadable(d: InstanceData): Boolean {
        return d.lineToken == data!!.lineToken && d.processName == data!!.processName
    }

    fun clearPreference() {
        try {
            preferences.clear()
            preferences.flush()
        } catch (e: BackingStoreException) {
            logger.warning(String.format("[%s]Failed to clear preference", data!!.processName))
        }
    }

    fun reload(newData: InstanceData): Boolean {
        if (!isReloadable(newData)) return false
        if (data!!.enabled && !newData.enabled) {
            sender.notifyDisabled()
        } else if (!data!!.enabled && newData.enabled) {
            sender.notifyEnabled()
        }
        init(newData)
        return true
    }

    @Synchronized
    @Throws(BackingStoreException::class, IOException::class)
    fun checkNoSubmission() {
        if (!data!!.enabled) return
        logger.info(String.format("[%s]Checking submission status...", data!!.processName))
        val nonResponders = connector.getCachedNoSubmission()
        for (userID in preferences.keys()) {
            val name = preferences[userID, null]
            if (Arrays.stream(nonResponders).noneMatch { s: String? -> s == name }) continue
            logger.info(String.format("[%s]Sending Late reminder to %s(%s)", data!!.processName, name, userID))
            sender.sendLateReminder(userID)
        }
        logger.info(String.format("[%s]All submission status checks are finished.", data!!.processName))
    }

    @Throws(BackingStoreException::class, IOException::class)
    fun processEvent(e: Event) {
        if (e is UnfollowEvent) {
            //UnfollowedEventで登録を削除。
            logger.info(String.format("[%s]User %s unfollowed the bot", data!!.processName, e.getSource().userId))
            preferences.remove(e.getSource().userId)
            preferences.flush()
            return
        }
        if (data!!.enabled) {
            if (e is FollowEvent) {
                logger.info(
                    String.format(
                        "[%s]User %s started following the bot",
                        data!!.processName,
                        e.getSource().userId
                    )
                )
                sender.sendWelcomeMessage(e.replyToken)
            } else if (e is MessageEvent<*>) {
                val token = e.replyToken
                val mes = e.message
                if (mes is TextMessageContent) {
                    val message = mes.text
                    processRequest(message, e.getSource().userId, token)
                } else {
                    sender.sendError(LineMessageSender.ErrorType.InvalidFormat, token)
                }
            }
        } else {
            if (e is FollowEvent) {
                sender.warnDisabled(e.replyToken)
            } else if (e is MessageEvent<*>) {
                sender.warnDisabled(e.replyToken)
            }
        }
    }

    fun broadcastReminder() {
        if (!isEnabled) return
        sender.broadcastReminder()
    }

    val processName: String?
        get() = data!!.processName
    val isEnabled: Boolean
        get() = data!!.enabled
    val lineUsage: Long
        get() = sender.usage
    val registeredNum: Long
        get() = try {
            preferences.keys().size.toLong()
        } catch (e: BackingStoreException) {
            logger.severe("ERROR in getRegisteredNum()")
            -1
        }

    fun requestNumFollowers(data: StatusData) {
        if (!isEnabled) return
        sender.requestNumFollowers(data)
    }
}