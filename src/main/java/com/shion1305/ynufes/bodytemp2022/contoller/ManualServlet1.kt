/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.contoller

import com.shion1305.ynufes.bodytemp2022.config.ConfigManager
import java.io.IOException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/maintenance/*")
class ManualServlet : HttpServlet() {
    @Throws(IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val pass = req.getParameter("pass")
        if (pass == null || pass != ConfigManager.getConfig(ConfigManager.ConfigProperty.MAINTENANCE_PASS)) {
            resp.status = 401
            resp.writer.write("パスワードが間違っています。")
            return
        }
        val url = req.pathInfo
        val action = url.substring(url.lastIndexOf("/") + 1)
        when (action) {
            "reload" -> {
                resp.status = 200
                resp.writer.write("reloaded!")
                ProcessorManager.Companion.reload()
            }

            else -> {
                resp.status = 400
                resp.writer.write("BAD REQUEST")
            }
        }
    }
}