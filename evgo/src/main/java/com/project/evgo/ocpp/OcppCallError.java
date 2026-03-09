package com.project.evgo.ocpp;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * OCPP 1.6J CALLERROR message (messageTypeId = 4).
 * <p>
 * JSON frame:
 * {@code [4, "<messageId>", "<errorCode>", "<errorDescription>", {<errorDetails>}]}
 *
 * @param messageId        unique identifier correlating with the original CALL
 * @param errorCode        one of the OCPP error codes (e.g. "NotImplemented",
 *                         "InternalError")
 * @param errorDescription human-readable error description
 * @param errorDetails     additional error details as a JSON object
 */
public record OcppCallError(
        String messageId,
        String errorCode,
        String errorDescription,
        JsonNode errorDetails) implements OcppMessage {

    private static final int MESSAGE_TYPE_ID = 4;

    @Override
    public int getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }
}
