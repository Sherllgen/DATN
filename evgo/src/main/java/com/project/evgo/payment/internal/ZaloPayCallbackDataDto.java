package com.project.evgo.payment.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Internal DTO for parsing the ZaloPay callback {@code data} JSON string.
 * Not a public API response — kept in the internal package intentionally.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ZaloPayCallbackDataDto(
        @JsonProperty("app_trans_id") String appTransId,
        @JsonProperty("zp_trans_id") String zpTransId,
        @JsonProperty("return_code") Integer returnCode) {
}
