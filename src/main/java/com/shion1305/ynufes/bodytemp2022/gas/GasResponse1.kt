/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */
package com.shion1305.ynufes.bodytemp2022.gas

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class GasResponse(
    @param:JsonProperty("code") var code: Int,
    @param:JsonProperty("message") var message: String,
    @param:JsonProperty("currentValue") var currentValue: String,
    @param:JsonProperty("notResponders") var notResponders: Array<String>
) {
    override fun toString(): String {
        return "GasResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", currentValue='" + currentValue + '\'' +
                ", notResponders=" + Arrays.toString(notResponders) +
                '}'
    }
}