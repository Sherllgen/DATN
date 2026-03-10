package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.internal.OcppActionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Handles the OCPP "Heartbeat" action.
 * <p>
 * Updates the charge point's last heartbeat timestamp and returns the current
 * server time.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HeartbeatHandler implements OcppActionHandler {

    private static final String ACTION = "Heartbeat";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ChargerService chargerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        log.debug("Heartbeat from {}", chargePointId);

        try {
            Long chargerId = Long.parseLong(chargePointId);
            chargerService.updateHeartbeat(chargerId);
        } catch (NumberFormatException e) {
            log.warn("Heartbeat from invalid charge point ID: {}", chargePointId);
        }

        ObjectNode responsePayload = objectMapper.createObjectNode();
        responsePayload.put("currentTime", Instant.now().atOffset(ZoneOffset.UTC)
                .format(ISO_FORMATTER));

        return new OcppCallResult(call.messageId(), responsePayload);
    }
}
