package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.ocpp.internal.OcppActionHandler;
import com.project.evgo.sharedkernel.enums.PortStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Handles the OCPP "StatusNotification" action.
 * <p>
 * Logs the status change from a connector and persists the port status
 * to the database via {@link ChargerService}.
 * Per OCPP 1.6 spec, the response (StatusNotification.conf) is an empty object.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatusNotificationHandler implements OcppActionHandler {

    private static final String ACTION = "StatusNotification";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChargerService chargerService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Maps OCPP 1.6 ChargePointStatus strings to our {@link PortStatus} enum.
     */
    private static final Map<String, PortStatus> STATUS_MAP = Map.of(
            "Available", PortStatus.AVAILABLE,
            "Preparing", PortStatus.PREPARING,
            "Charging", PortStatus.CHARGING,
            "SuspendedEVSE", PortStatus.SUSPENDED_EVSE,
            "SuspendedEV", PortStatus.SUSPENDED_EV,
            "Finishing", PortStatus.FINISHING,
            "Reserved", PortStatus.RESERVED,
            "Unavailable", PortStatus.UNAVAILABLE,
            "Faulted", PortStatus.FAULTED
    );

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        JsonNode payload = call.payload();

        int connectorId = payload.path("connectorId").asInt(0);
        String status = payload.path("status").asText("Unknown");
        String errorCode = payload.path("errorCode").asText("NoError");

        log.info("StatusNotification from CP {}: connector={}, status={}, errorCode={}",
                chargePointId, connectorId, status, errorCode);

        // Persist port status to DB
        PortStatus portStatus = STATUS_MAP.getOrDefault(status, PortStatus.UNAVAILABLE);
        try {
            // chargePointId is the charger's database ID (as string)
            Long chargerId = Long.parseLong(chargePointId);
            List<PortResponse> ports = chargerService.findPortsByChargerId(chargerId);
            ports.stream()
                    .filter(p -> p.getPortNumber() == connectorId)
                    .findFirst()
                    .ifPresent(p -> {
                        chargerService.internalUpdatePortStatus(p.getId(), portStatus);
                        log.info("Updated port status: chargerId={}, portNumber={}, status={}",
                                chargerId, connectorId, portStatus);
                    });
        } catch (NumberFormatException e) {
            log.warn("Could not parse chargePointId '{}' as Long, skipping DB update", chargePointId);
        }

        // OCPP 1.6 StatusNotification.conf is an empty object
        ObjectNode confPayload = objectMapper.createObjectNode();

        // Publish event for downstream modules like Charging Module
        String info = payload.path("info").asText(null);
        String timestampStr = payload.path("timestamp").asText(null);
        LocalDateTime timestamp = null;
        if (timestampStr != null) {
            try {
                timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse timestamp in StatusNotification: {}", timestampStr);
            }
        }
        String vendorErrorCode = payload.path("vendorErrorCode").asText(null);
        String vendorId = payload.path("vendorId").asText(null);

        Long resolvedPortId = null;
        try {
            Long chargerId = Long.parseLong(chargePointId);
            List<PortResponse> ports = chargerService.findPortsByChargerId(chargerId);
            resolvedPortId = ports.stream()
                .filter(p -> p.getPortNumber().equals(connectorId))
                .map(PortResponse::getId)
                .findFirst()
                .orElse(null);
        } catch (NumberFormatException ignored) {
        }
        
        eventPublisher.publishEvent(new StatusNotificationReceivedEvent(
                chargePointId,
                connectorId,
                resolvedPortId,
                errorCode,
                status,
                info,
                timestamp,
                vendorErrorCode,
                vendorId
        ));

        return new OcppCallResult(call.messageId(), confPayload);
    }
}
