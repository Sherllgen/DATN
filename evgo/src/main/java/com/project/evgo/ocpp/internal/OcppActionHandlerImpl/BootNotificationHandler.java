package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.internal.OcppActionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Handles the OCPP "BootNotification" action.
 * <p>
 * Validates the charge point against the database, updates metadata
 * (vendor, model, serial, firmware), and returns an Accepted/Rejected response.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BootNotificationHandler implements OcppActionHandler {

    private static final String ACTION = "BootNotification";
    private static final int HEARTBEAT_INTERVAL_SECONDS = 300;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ChargerService chargerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        JsonNode payload = call.payload();

        String vendor = payload.path("chargePointVendor").asText("");
        String model = payload.path("chargePointModel").asText("");
        String serial = payload.path("chargePointSerialNumber").asText(null);
        String firmware = payload.path("firmwareVersion").asText(null);

        log.info("BootNotification from {}: vendor={}, model={}", chargePointId, vendor, model);

        // Validate required fields per OCPP 1.6 spec
        if (vendor.isBlank() || model.isBlank()) {
            log.warn("BootNotification from {} missing required fields (vendor or model)", chargePointId);
            return createResponse(call.messageId(), "Rejected");
        }

        // Parse chargePointId as Long (charger database ID)
        Long chargerId;
        try {
            chargerId = Long.parseLong(chargePointId);
        } catch (NumberFormatException e) {
            log.warn("BootNotification from invalid charge point ID: {}", chargePointId);
            return createResponse(call.messageId(), "Rejected");
        }

        // Lookup and update charger in DB
        Optional<ChargerResponse> result = chargerService.processBootNotification(
                chargerId, vendor, model, serial, firmware);

        if (result.isEmpty()) {
            log.warn("BootNotification from unknown charge point ID: {}", chargerId);
            return createResponse(call.messageId(), "Rejected");
        }

        log.info("BootNotification accepted for charge point ID: {}", chargerId);
        return createResponse(call.messageId(), "Accepted");
    }

    private OcppCallResult createResponse(String messageId, String status) {
        ObjectNode responsePayload = objectMapper.createObjectNode();
        responsePayload.put("status", status);
        responsePayload.put("currentTime", Instant.now().atOffset(ZoneOffset.UTC)
                .format(ISO_FORMATTER));
        responsePayload.put("interval", HEARTBEAT_INTERVAL_SECONDS);
        return new OcppCallResult(messageId, responsePayload);
    }
}
