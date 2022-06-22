/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GasResponse {
    public int code;
    public String message;
    public String currentValue;
    public String[] notResponders;

    public GasResponse(@JsonProperty("code") int code,
                       @JsonProperty("message") String message,
                       @JsonProperty("currentValue") String currentValue,
                       @JsonProperty("notResponders") String[] notResponders) {
        this.code = code;
        this.message = message;
        this.currentValue = currentValue;
        this.notResponders = notResponders;
    }

    @Override
    public String toString() {
        return "GasResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", currentValue='" + currentValue + '\'' +
                ", notResponders=" + Arrays.toString(notResponders) +
                '}';
    }
}
