/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.contoller

import com.linecorp.bot.model.event.Event
import com.shion1305.ynufes.bodytemp2022.config.JsonConfigManager
import java.io.IOException
import java.util.logging.Logger
import java.util.prefs.BackingStoreException
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class ProcessorManager : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent) {
        init()
    }

    object StatusDataManager {
        private var data: StatusDataGroup? = null

        @get:Synchronized
        val statusData: StatusDataGroup?
            get() {
                if (data == null || System.currentTimeMillis() - data!!.time > 60000) {
                    data = updateStatusData()
                    logger.info("UPDATED")
                }
                return data
            }

        private fun updateStatusData(): StatusDataGroup {
            val newData = StatusDataGroup()
            processors.values.stream().parallel().forEach { p: RequestProcessor? ->
                val statusData = StatusData(p.getProcessName(), p!!.isEnabled, p.lineUsage, p.registeredNum)
                p.requestNumFollowers(statusData)
                newData.addStatus(statusData)
            }
            newData.time = System.currentTimeMillis()
            return newData
        }
    }

    class StatusDataGroup {
        val data = ArrayList<StatusData>()
        var time = 0L
        fun addStatus(d: StatusData) {
            data.add(d)
        }
    }

    class StatusData(var processName: String?, var enabled: Boolean, var usage: Long, var registered: Long) {
        var numFollowers = LineNumInfo()

        class LineNumInfo {
            enum class Status {
                READY, NOT_READY, ERROR, PROCESSING
            }

            var status = Status.PROCESSING
            var followers: Long = 0
            var blockers: Long = 0
            var targetReaches: Long = 0
        }
    }

    companion object {
        var logger = Logger.getLogger("ProcessManager")
        var processors = HashMap<String, RequestProcessor>()
        private fun init() {
            val data = JsonConfigManager.readJson() ?: return
            for (d in data) {
                processors[d!!.processName] = RequestProcessor(d)
                logger.info(String.format("[%s]Process Registered", d.processName))
            }
        }

        /**
         * 設定の再読み込みを行う。
         * jsonファイルからボット設定が消去された場合、preferenceは削除されずにインスタンスのみ削除される。
         * reload処理であるが、reloadableではない(lineTokenが違う)場合は、preferenceが削除され、
         * インスタンスが再生成される。
         */
        fun reload() {
            val data = JsonConfigManager.readJson() ?: return
            val newProcessors = HashMap<String?, RequestProcessor?>()
            for (d in data) {
                if (processors.containsKey(d!!.processName)) {
                    val process = processors[d.processName]
                    if (process!!.reload(d)) {
                        newProcessors[d.processName] = process
                        logger.info(String.format("[%s]Process Reloaded, %b", d.processName, d.enabled))
                        continue
                    } else {
                        process.clearPreference()
                    }
                }
                newProcessors[d.processName] = RequestProcessor(d)
                logger.info(String.format("[%s]Process Registered", d.processName))
            }
            processors.clear()
            processors = newProcessors
        }

        @Throws(BackingStoreException::class, IOException::class)
        fun processEvent(reqName: String, e: Event) {
            val processor = processors[reqName]
            if (processor != null) processor.processEvent(e) else logger.info("Received unknown event: reqName: $reqName")
        }

        fun broadcastReminder() {
            for (processor in processors.values) {
                processor!!.broadcastReminder()
            }
        }

        @Synchronized
        @Throws(BackingStoreException::class, IOException::class)
        fun checkNoSubmission() {
            for (processor in processors.values) {
                processor!!.checkNoSubmission()
            }
        }
    }
}