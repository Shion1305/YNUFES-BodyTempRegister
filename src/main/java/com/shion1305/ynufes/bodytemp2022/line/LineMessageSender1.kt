/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.line

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.Broadcast
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.flex.component.Box
import com.linecorp.bot.model.message.flex.component.FlexComponent
import com.linecorp.bot.model.message.flex.component.Text
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.unit.FlexFontSize
import com.linecorp.bot.model.message.flex.unit.FlexLayout
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize
import com.linecorp.bot.model.response.GetNumberOfFollowersResponse
import com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager.StatusData
import com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager.StatusData.LineNumInfo
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Logger

class LineMessageSender(private val processName: String, botToken: String?) {
    private val client: LineMessagingClient

    init {
        client = LineMessagingClient.builder(botToken).build()
    }

    private fun broadcast(message: Message) {
        try {
            client.broadcast(Broadcast(message)).get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }

    private fun reply(replyToken: String, message: Message) {
        try {
            client.replyMessage(ReplyMessage(replyToken, message))[30, TimeUnit.SECONDS]
        } catch (e: ExecutionException) {
            logger.severe(String.format("[%s]ERROR in sending Message", processName))
            e.printStackTrace()
        } catch (e: TimeoutException) {
            logger.severe(String.format("[%s]ERROR in sending Message", processName))
            e.printStackTrace()
        } catch (e: InterruptedException) {
            logger.severe(String.format("[%s]ERROR in sending Message", processName))
            e.printStackTrace()
        }
    }

    private fun push(message: PushMessage) {
        client.pushMessage(message)
    }

    enum class ErrorType {
        InvalidFormat, NAME_NOT_FOUND, INVALID_TEMP_FORMAT, SERVER_ERROR, ERROR_UNKNOWN
    }

    fun sendWelcomeMessage(replyToken: String) {
        val message =
            standardMessage("体温入力BOTへようこそ", "まずはじめに\n名前をスペースを入れずに入力してください(例:市川詩恩)", "体温入力BOTへようこそ")
        reply(replyToken, message)
    }

    fun nameSuccess(name: String, token: String) {
        val title = createLabel("名前を登録しました", true)
        val description = createLabel("以下の内容で登録しました", false)
        val data: MutableMap<String, String> = HashMap()
        data["名前"] = name
        val mainBox = Box.builder().layout(FlexLayout.VERTICAL)
            .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build()
        reply(
            token, FlexMessage.builder()
                .contents(Bubble.builder().body(mainBox).build())
                .altText("名前登録完了").build()
        )
    }

    fun tempSuccess(temp: String, name: String, replyToken: String) {
        val title = createLabel("体温を登録しました", true)
        val description = createLabel("以下の内容で登録しました", false)
        val data: MutableMap<String, String> = HashMap()
        data["名前"] = name
        data["体温"] = temp
        data["日付"] = SimpleDateFormat("MM/dd").format(Date())
        val mainBox = Box.builder().layout(FlexLayout.VERTICAL)
            .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build()
        val bubble = Bubble.builder().body(mainBox).build()
        val message = FlexMessage.builder().contents(bubble).altText("体温登録成功").build()
        reply(replyToken, message)
    }

    private fun listItems(data: Map<String, String>): Box {
        val contents: MutableList<FlexComponent> = ArrayList()
        for ((key, value) in data) {
            contents.add(
                Box.builder().layout(FlexLayout.BASELINE)
                    .spacing(FlexMarginSize.SM)
                    .contents(
                        Text.builder().text(key).flex(1).size(FlexFontSize.SM).color("#aaaaaa").build(),
                        Text.builder().text(value).flex(4).size(FlexFontSize.SM).color("#666666").build()
                    )
                    .build()
            )
        }
        return Box.builder().layout(FlexLayout.VERTICAL)
            .contents(contents).spacing(FlexMarginSize.SM).build()
    }

    fun sendError(t: ErrorType, replyToken: String) {
        val mes: String
        mes = when (t) {
            ErrorType.InvalidFormat -> "不正な形式のメッセージです。テキストメッセージのみ有効です。"
            ErrorType.INVALID_TEMP_FORMAT -> "体温の形式が正しくありません。正しく入力してください(例: 36.0)"
            ErrorType.SERVER_ERROR -> "SpreadSheet側でエラーが発生しました。\nSpreadSheet側で今日の欄が登録されていない可能性があります。編集部市川までご連絡ください。"
            ErrorType.NAME_NOT_FOUND -> "SpreadSheet上で入力された名前は見つかりませんでした。スペースを入れずに名前を入力してください(例:市川詩恩)。解決しない場合は編集部市川まで"
            ErrorType.ERROR_UNKNOWN -> "不明のエラーが発生しました。"
            else -> "不明のエラーが発生しました。"
        }
        logger.info(String.format("[%s]ERROR, %s", processName, t.name))
        val title = createLabel("エラー", true)
        val content = createLabel(mes, false)
        val box = Box.builder().contents(title, content).layout(FlexLayout.VERTICAL).build()
        val b = Bubble.builder().body(box).build()
        val message = FlexMessage.builder().contents(b).altText("エラーメッセージ").build()
        reply(replyToken, message)
    }

    fun broadcastReminder() {
        val message = standardMessage("検温忘れずに!", "検温しましょう!このチャットに体温を送信してください!(例:36.4)", "検温リマインダー")
        broadcast(message)
        logger.info(String.format("[%s]Reminder broadcasted", processName))
    }

    fun standardMessage(title: String?, content: String?, altText: String?): FlexMessage {
        val b = Box.builder()
            .layout(FlexLayout.VERTICAL)
            .contents(createLabel(title, true), createLabel(content, false)).build()
        val bubble = Bubble.builder().body(b).build()
        return FlexMessage.builder()
            .contents(bubble)
            .altText(altText)
            .build()
    }

    fun sendLateReminder(userID: String?) {
        val message = standardMessage("入力を忘れていませんか?", "検温入力が御済みでないようです!忘れずに入力しましょう!", "検温入力忘れていませんか?")
        push(PushMessage(userID, message))
    }

    fun warnDisabled(replyToken: String) {
        val message = standardMessage("稼働停止中", "このBOTは現在稼働停止中です。\n詳細は編集部市川まで", "稼働停止中")
        reply(replyToken, message)
    }

    fun notifyDisabled() {
        val message = standardMessage("稼働停止しました", "このBOTは無効化されました。\n詳細は編集部市川まで", "稼働停止通知")
        broadcast(message)
    }

    fun notifyEnabled() {
        val message = standardMessage("稼働再開しました", "このBOTは稼働を再開しました。", "稼働再開通知")
        broadcast(message)
    }

    val usage: Long
        get() = try {
            client.messageQuotaConsumption.get().totalUsage
        } catch (e: InterruptedException) {
            logger.warning("Failed to get usage data")
            -1
        } catch (e: ExecutionException) {
            logger.warning("Failed to get usage data")
            -1
        }

    fun requestNumFollowers(data: StatusData) {
        client.getNumberOfFollowers(
            SimpleDateFormat("yyyyMMdd").format(
                Instant.now()
                    .toEpochMilli()
            )
        )
            .whenComplete { num: GetNumberOfFollowersResponse, ex: Throwable? ->
                if (ex != null) {
                    logger.warning("Failed to get number of followers")
                    data.numFollowers.status = LineNumInfo.Status.ERROR
                } else {
                    if (num.status.name == "READY") {
                        data.numFollowers.status = LineNumInfo.Status.READY
                        data.numFollowers.followers = num.followers
                        data.numFollowers.blockers = num.blocks
                        data.numFollowers.targetReaches = num.targetedReaches
                    } else {
                        data.numFollowers.status = LineNumInfo.Status.NOT_READY
                    }
                }
            }
    }

    companion object {
        private val logger = Logger.getLogger("MessageSender")
        fun createLabel(s: String?, bold: Boolean): Text {
            return Text.builder().text(s).weight(if (bold) Text.TextWeight.BOLD else Text.TextWeight.REGULAR).wrap(true)
                .build()
        }
    }
}