/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.gas

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.logging.Logger

class GASConnector internal constructor(var url: String) {
    var cacheTime = 0L
    var cacheNoSubmission: Array<String?>?
    @Throws(IOException::class)
    fun checkName(name: String?): Boolean {
        val resp = check(name)
        return resp.code == 200
    }

    @Throws(IOException::class)
    fun check(name: String?): GasResponse {
        val requestUrl = StringBuilder(url)
        requestUrl.append("?type=check&name=")
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
        return sendRequest(requestUrl.toString())
    }

    @Throws(IOException::class)
    fun checkRecord(name: String?): String? {
        val resp = check(name)
        return resp.currentValue
    }

    @Throws(IOException::class)
    fun register(name: String?, bodyTemp: String?): Int {
        val requestUrl = StringBuilder(url)
        requestUrl.append("?type=register&name=")
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
        requestUrl.append('&')
        requestUrl.append("temp=")
        requestUrl.append(bodyTemp)
        return sendRequest(requestUrl.toString()).code
    }

    @Throws(IOException::class)
    private fun sendRequest(req: String): GasResponse {
        val connection = URI.create(req).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        val mapper = ObjectMapper()
        return mapper.readValue(connection.inputStream, GasResponse::class.java)
    }

    @get:Throws(IOException::class)
    val cachedNoSubmission: Array<String?>?
        get() = if (System.currentTimeMillis() - cacheTime < 10000) {
            cacheNoSubmission
        } else noSubmission

    @get:Throws(IOException::class)
    @get:Synchronized
    val noSubmission: Array<String?>?
        get() {
            val response = ObjectMapper().readValue(URL("$url?type=listNotResponded"), GasResponse::class.java)
            cacheTime = System.currentTimeMillis()
            cacheNoSubmission = response.notResponders
            logger.info(String.format("NoSubmissionCheck: %s", Arrays.toString(response.notResponders)))
            return response.notResponders
        }

    companion object {
        private val logger = Logger.getLogger("GASConnector")
    }
}