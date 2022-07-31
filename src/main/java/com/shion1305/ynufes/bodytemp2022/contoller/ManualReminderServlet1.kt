/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.contoller

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/sendReminder")
class ManualReminderServlet : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        ProcessorManager.Companion.broadcastReminder()
    }
}