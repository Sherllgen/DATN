package com.project.evgo.ocpp;
import com.fasterxml.jackson.databind.JsonNode;
/**
 * OCPP 1.6J CALL message (messageTypeId = 2).
 * <p>
 * JSON frame: {@code [2, "<messageId>", "<action>", {<payload>}]}
 *
 * @param messageId unique identifier correlating request/response
 * @param action    the OCPP action name (e.g. "BootNotification", "Heartbeat")
 * @param payload   the JSON payload object
 */
public record OcppCall(
        String messageId,
        String action,
        JsonNode payload
) implements OcppMessage {
    private static final int MESSAGE_TYPE_ID = 2;
    @Override
    public int getMessageTypeId() {
        return MESSAGE_TYPE_ID;
    }
}
