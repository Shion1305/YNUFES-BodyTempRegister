/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.gas

object GASManager {
    var connectors = HashMap<String, GASConnector>()
    fun getGASConnector(url: String?): GASConnector? {
        if (!connectors.containsKey(url)) {
            connectors[url] = GASConnector(url)
        }
        return connectors[url]
    }
}