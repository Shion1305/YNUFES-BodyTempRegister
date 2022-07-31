/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.contoller

import java.io.IOException
import java.util.*
import java.util.logging.Logger
import java.util.prefs.BackingStoreException
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class ReminderSender : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent) {
        schedule()
        scheduleLateReminder()
    }

    override fun contextDestroyed(sce: ServletContextEvent) {
        timer.purge()
    }

    companion object {
        var logger = Logger.getLogger("TempInputReminder")

        /*
    Schedule at next 07:00
     */
        var timer = Timer()
        fun schedule() {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            if (hour > 6) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.HOUR_OF_DAY] = 7
            calendar[Calendar.SECOND] = 0
            val d = calendar.time
            logger.info("Scheduled at $d")
            timer.schedule(object : TimerTask() {
                override fun run() {
                    ProcessorManager.broadcastReminder()
                    logger.info("The scheduled reminder has been broadcasted")
                    schedule()
                }
            }, d)
        }

        //15時と20時に再度リマインド
        fun scheduleLateReminder() {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            if (hour < 15) {
                calendar[Calendar.HOUR_OF_DAY] = 15
            } else if (hour < 20) {
                calendar[Calendar.HOUR_OF_DAY] = 20
            } else if (hour < 23) {
                calendar[Calendar.HOUR_OF_DAY] = 23
            } else {
                calendar[Calendar.HOUR_OF_DAY] = 15
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            val d1 = calendar.time
            logger.info("Late Reminder Scheduled at $d1")
            timer.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        ProcessorManager.checkNoSubmission()
                    } catch (e: BackingStoreException) {
                        throw RuntimeException(e)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    scheduleLateReminder()
                    logger.info("Scheduled late reminder")
                }
            }, d1)
        }
    }
}