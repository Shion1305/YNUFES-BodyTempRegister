/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceData {
    public String name;
    public String gasUrl;
    public String lineToken;

    public InstanceData(@JsonProperty("name") String name, @JsonProperty("GASUrl") String getGasUrl, @JsonProperty("LineMessagingToken") String token) {
        this.name = name;
        this.gasUrl = getGasUrl;
        this.lineToken = token;
    }
}
