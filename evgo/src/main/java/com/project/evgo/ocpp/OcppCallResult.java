package com.project.evgo.ocpp;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * OCPP 1.6J CALLRESULT message (messageTypeId = 3).
 * <p>
 * JSON frame: {@code [3, "<messageId>", {<payload>}]}
 *
 * @param messageId unique identifier correlating with the original CALL
 * @param payload   the JSON response payload object
 */
public record OcppCallResult(
        String messageId,
        JsonNode payload) implements OcppMessage {

    private static final int MESSAGE_TYPE_ID = 3;

    @Override
    public int getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }
}
