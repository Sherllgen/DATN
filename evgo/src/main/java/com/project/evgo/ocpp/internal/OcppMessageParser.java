package com.project.evgo.ocpp.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallError;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.OcppErrorCode;
import com.project.evgo.ocpp.OcppMessage;
import org.springframework.stereotype.Component;

/**
 * Parses raw OCPP 1.6J JSON messages into typed {@link OcppMessage} objects
 * and serializes {@link OcppMessage} objects back to JSON strings.
 */
@Component
public class OcppMessageParser {

    private static final int CALL_TYPE_ID = 2;
    private static final int CALL_RESULT_TYPE_ID = 3;
    private static final int CALL_ERROR_TYPE_ID = 4;

    private static final int CALL_MIN_ELEMENTS = 4;
    private static final int CALL_RESULT_MIN_ELEMENTS = 3;
    private static final int CALL_ERROR_MIN_ELEMENTS = 5;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a raw JSON string into an {@link OcppMessage}.
     *
     * @param rawJson the raw JSON message string
     * @return the parsed OcppMessage (OcppCall, OcppCallResult, or OcppCallError)
     * @throws OcppProtocolException if the message cannot be parsed
     */
    public OcppMessage parse(String rawJson) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(rawJson);
        } catch (JsonProcessingException exception) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "Failed to parse JSON: " + exception.getMessage(),
                    exception);
        }

        if (!rootNode.isArray()) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "OCPP message must be a JSON array");
        }

        ArrayNode arrayNode = (ArrayNode) rootNode;
        if (arrayNode.isEmpty()) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "OCPP message array must not be empty");
        }

        int messageTypeId = arrayNode.get(0).asInt();

        return switch (messageTypeId) {
            case CALL_TYPE_ID -> parseCall(arrayNode);
            case CALL_RESULT_TYPE_ID -> parseCallResult(arrayNode);
            case CALL_ERROR_TYPE_ID -> parseCallError(arrayNode);
            default -> throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "Unknown message type ID: " + messageTypeId);
        };
    }

    /**
     * Serializes an {@link OcppMessage} to its JSON string representation.
     *
     * @param message the OcppMessage to serialize
     * @return the JSON string
     */
    public String serialize(OcppMessage message) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        switch (message) {
            case OcppCall call -> {
                arrayNode.add(call.getMessageTypeId());
                arrayNode.add(call.messageId());
                arrayNode.add(call.action());
                arrayNode.add(call.payload());
            }
            case OcppCallResult callResult -> {
                arrayNode.add(callResult.getMessageTypeId());
                arrayNode.add(callResult.messageId());
                arrayNode.add(callResult.payload());
            }
            case OcppCallError callError -> {
                arrayNode.add(callError.getMessageTypeId());
                arrayNode.add(callError.messageId());
                arrayNode.add(callError.errorCode());
                arrayNode.add(callError.errorDescription());
                arrayNode.add(callError.errorDetails());
            }
        }

        try {
            return objectMapper.writeValueAsString(arrayNode);
        } catch (JsonProcessingException exception) {
            throw new OcppProtocolException(
                    OcppErrorCode.INTERNAL_ERROR,
                    "Failed to serialize OCPP message: " + exception.getMessage(),
                    exception);
        }
    }

    private OcppCall parseCall(ArrayNode arrayNode) {
        if (arrayNode.size() < CALL_MIN_ELEMENTS) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "CALL message must have at least " + CALL_MIN_ELEMENTS + " elements, got " + arrayNode.size());
        }

        String messageId = arrayNode.get(1).asText();
        String action = arrayNode.get(2).asText();
        JsonNode payload = arrayNode.get(3);

        return new OcppCall(messageId, action, payload);
    }

    private OcppCallResult parseCallResult(ArrayNode arrayNode) {
        if (arrayNode.size() < CALL_RESULT_MIN_ELEMENTS) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "CALLRESULT message must have at least " + CALL_RESULT_MIN_ELEMENTS + " elements, got "
                            + arrayNode.size());
        }

        String messageId = arrayNode.get(1).asText();
        JsonNode payload = arrayNode.get(2);

        return new OcppCallResult(messageId, payload);
    }

    private OcppCallError parseCallError(ArrayNode arrayNode) {
        if (arrayNode.size() < CALL_ERROR_MIN_ELEMENTS) {
            throw new OcppProtocolException(
                    OcppErrorCode.FORMATION_VIOLATION,
                    "CALLERROR message must have at least " + CALL_ERROR_MIN_ELEMENTS + " elements, got "
                            + arrayNode.size());
        }

        String messageId = arrayNode.get(1).asText();
        String errorCode = arrayNode.get(2).asText();
        String errorDescription = arrayNode.get(3).asText();
        JsonNode errorDetails = arrayNode.get(4);

        return new OcppCallError(messageId, errorCode, errorDescription, errorDetails);
    }
}
