/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.gas;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class GASConnector {
    String url;
    long cacheTime = 0L;
    String[] cacheNoSubmission;
    private static final Logger logger = Logger.getLogger("GASConnector");

    GASConnector(String url) {
        this.url = url;
    }

    public boolean checkName(String name) throws IOException {
        GasResponse resp = check(name);
        return resp.code == 200;
    }

    public GasResponse check(String name) throws IOException {
        StringBuilder requestUrl = new StringBuilder(url);
        requestUrl.append("?type=check&name=");
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        return sendRequest(requestUrl.toString());
    }

    public String checkRecord(String name) throws IOException {
        GasResponse resp = check(name);
        return resp.currentValue;
    }

    public int register(String name, String bodyTemp) throws IOException {
        StringBuilder requestUrl = new StringBuilder(url);
        requestUrl.append("?type=register&name=");
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        requestUrl.append('&');
        requestUrl.append("temp=");
        requestUrl.append(bodyTemp);
        try {
            return sendRequest(requestUrl.toString()).code;
        } catch (IOException e) {
            logger.severe("Failed to register");
            throw e;
        }
    }

    private GasResponse sendRequest(String req) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(req).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(connection.getInputStream(), GasResponse.class);
    }

    public String[] getCachedNoSubmission() throws IOException {
        if (System.currentTimeMillis() - cacheTime < 10000) {
            return cacheNoSubmission;
        }
        return getNoSubmission();
    }

    public synchronized String[] getNoSubmission() throws IOException {
        GasResponse response = new ObjectMapper().readValue(new URL(url + "?type=listNotResponded"), GasResponse.class);
        cacheTime = System.currentTimeMillis();
        cacheNoSubmission = response.notResponders;
        logger.info(String.format("NoSubmissionCheck: %s", Arrays.toString(response.notResponders)));
        return response.notResponders;
    }
}
