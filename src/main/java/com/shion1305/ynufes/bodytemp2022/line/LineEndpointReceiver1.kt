/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.line

import com.linecorp.bot.parser.WebhookParseException
import com.linecorp.bot.parser.WebhookParser
import com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager
import java.io.IOException
import java.util.logging.Logger
import java.util.prefs.BackingStoreException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/line/*")
class LineEndpointReceiver : HttpServlet() {
    @Throws(IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val url = req.pathInfo
        if (url.endsWith("/")) {
            resp.status = 400
            return
        }
        val reqName = url.substring(url.lastIndexOf('/') + 1)
        val parser = WebhookParser { content: ByteArray?, headerSignature: String? -> true }
        try {
            val request = parser.handle("teset", req.inputStream.readAllBytes())
            val events = request.events
            for (event in events) {
                ProcessorManager.Companion.processEvent(reqName, event)
            }
        } catch (e: WebhookParseException) {
            throw RuntimeException(e)
        } catch (e: BackingStoreException) {
            throw RuntimeException(e)
        }
        resp.status = 200
    }

    companion object {
        private val logger = Logger.getLogger("LineEndpointReceiver")
    }
}