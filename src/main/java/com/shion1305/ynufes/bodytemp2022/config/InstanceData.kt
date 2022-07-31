/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class InstanceData(
    @param:JsonProperty("name") var processName: String,
    @param:JsonProperty("GASUrl") var gasUrl: String,
    @param:JsonProperty(
        "LineMessagingToken"
    ) var lineToken: String,
    @field:Volatile @param:JsonProperty("enabled") var enabled: Boolean
)