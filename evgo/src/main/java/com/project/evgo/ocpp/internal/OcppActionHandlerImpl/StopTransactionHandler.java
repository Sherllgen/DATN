package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.StopTransactionReceivedEvent;
import com.project.evgo.ocpp.internal.OcppActionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles the OCPP "StopTransaction" action.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StopTransactionHandler implements OcppActionHandler {

    private static final String ACTION = "StopTransaction";
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        JsonNode payload = call.payload();

        int transactionId = payload.path("transactionId").asInt();
        int meterStop = payload.path("meterStop").asInt();
        String timestampStr = payload.path("timestamp").asText();
        LocalDateTime timestamp = null;
        try {
            timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse StopTransaction timestamp: {}", timestampStr);
            timestamp = LocalDateTime.now();
        }

        String idTag = payload.has("idTag") ? payload.path("idTag").asText() : null;
        String reason = payload.has("reason") ? payload.path("reason").asText() : null;

        log.info("StopTransaction from CP {}: transactionId={}, meterStop={}, timestamp={}, reason={}",
                chargePointId, transactionId, meterStop, timestamp, reason);

        // Publish event for Charging module
        eventPublisher.publishEvent(new StopTransactionReceivedEvent(
                transactionId,
                meterStop,
                timestamp,
                idTag,
                reason
        ));

        // Create StopTransaction.conf
        ObjectNode confPayload = objectMapper.createObjectNode();
        if (idTag != null) {
            ObjectNode idTagInfo = objectMapper.createObjectNode();
            idTagInfo.put("status", "Accepted");
            confPayload.set("idTagInfo", idTagInfo);
        }

        return new OcppCallResult(call.messageId(), confPayload);
    }
}
