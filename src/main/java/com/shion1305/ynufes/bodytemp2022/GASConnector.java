package com.shion1305.ynufes.bodytemp2022;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GASConnector {
    String url;

    public GASConnector(String url) {
        this.url = url;
    }

    public boolean check(String name) throws IOException {
        StringBuilder requestUrl = new StringBuilder(url);
        requestUrl.append("?type=check&name=");
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        return sendRequest(requestUrl.toString()) == 200;
    }

    public int register(String name, String bodyTemp) throws IOException {
        StringBuilder requestUrl = new StringBuilder(url);
        requestUrl.append("?type=register&name=");
        requestUrl.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        requestUrl.append('&');
        requestUrl.append("temp=");
        requestUrl.append(bodyTemp);
        return sendRequest(requestUrl.toString());
    }

    private int sendRequest(String req) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(req).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        ObjectMapper mapper = new ObjectMapper();
        GasResponse response = mapper.readValue(connection.getInputStream(), GasResponse.class);
        return response.code;
    }
}
