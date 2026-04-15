package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import com.project.evgo.ocpp.internal.OcppActionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles the OCPP "StartTransaction" action.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartTransactionHandler implements OcppActionHandler {

    private static final String ACTION = "StartTransaction";
    private final ObjectMapper objectMapper;
    private final ChargerService chargerService;
    private final ApplicationEventPublisher eventPublisher;
    
    // Simple mock sequence for transaction IDs (in production, use DB sequence)
    private static final AtomicInteger transactionIdSeq = new AtomicInteger(1000);

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        JsonNode payload = call.payload();

        int connectorId = payload.path("connectorId").asInt();
        String idTag = payload.path("idTag").asText();
        int meterStart = payload.path("meterStart").asInt();
        String timestampStr = payload.path("timestamp").asText();
        LocalDateTime timestamp = null;
        try {
            timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse StartTransaction timestamp: {}", timestampStr);
            timestamp = LocalDateTime.now();
        }
        
        Integer reservationId = payload.has("reservationId") ? payload.path("reservationId").asInt() : null;

        int transactionId = transactionIdSeq.incrementAndGet();

        log.info("StartTransaction from CP {}: connectorId={}, idTag={}, meterStart={}, timestamp={}",
                chargePointId, connectorId, idTag, meterStart, timestamp);

        Long portId = null;
        try {
            Long chargerId = Long.parseLong(chargePointId);
            List<PortResponse> ports = chargerService.findPortsByChargerId(chargerId);
            portId = ports.stream()
                    .filter(p -> p.getPortNumber().equals(connectorId))
                    .map(PortResponse::getId)
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            log.warn("Could not parse chargePointId '{}' as Long", chargePointId);
        }

        // Publish event for Charging module
        eventPublisher.publishEvent(new StartTransactionReceivedEvent(
                chargePointId,
                connectorId,
                portId,
                transactionId,
                idTag,
                meterStart,
                timestamp,
                reservationId
        ));

        // Create StartTransaction.conf
        ObjectNode confPayload = objectMapper.createObjectNode();
        ObjectNode idTagInfo = objectMapper.createObjectNode();
        idTagInfo.put("status", "Accepted"); // Simulating always accepted for now
        
        confPayload.set("idTagInfo", idTagInfo);
        confPayload.put("transactionId", transactionId);

        return new OcppCallResult(call.messageId(), confPayload);
    }
}
