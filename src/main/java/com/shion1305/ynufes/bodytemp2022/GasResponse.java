/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GasResponse {
    public int code;
    public String message;
    public String currentValue;

    public GasResponse(@JsonProperty("code") int code,
                       @JsonProperty("message") String message,
                       @JsonProperty("currentValue") String currentValue) {
        this.code = code;
        this.message = message;
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        return "GasResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", currentValue='" + currentValue + '\'' +
                '}';
    }
}
