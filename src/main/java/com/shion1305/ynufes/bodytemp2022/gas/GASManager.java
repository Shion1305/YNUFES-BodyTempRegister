/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.gas;

import java.util.HashMap;

public class GASManager {
    static HashMap<String, GASConnector> connectors = new HashMap<>();

    public static GASConnector getGASConnector(String url) {
        if (!connectors.containsKey(url)) {
            connectors.put(url, new GASConnector(url));
        }
        return connectors.get(url);
    }
}

