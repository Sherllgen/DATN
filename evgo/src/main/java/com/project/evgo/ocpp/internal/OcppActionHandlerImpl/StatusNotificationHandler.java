package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.internal.OcppActionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the OCPP "StatusNotification" action.
 * <p>
 * Logs the status change from a connector. Per OCPP 1.6 spec,
 * the response (StatusNotification.conf) is an empty object.
 */
@Component
@Slf4j
public class StatusNotificationHandler implements OcppActionHandler {

    private static final String ACTION = "StatusNotification";
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // OCPP 1.6 StatusNotification.conf is an empty object
        return new OcppCallResult(call.messageId(), objectMapper.createObjectNode());
    }
}
