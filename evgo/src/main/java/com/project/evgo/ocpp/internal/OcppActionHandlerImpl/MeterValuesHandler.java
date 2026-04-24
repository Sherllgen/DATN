package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.project.evgo.ocpp.MeterValuesReceivedEvent;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.internal.OcppActionHandler;

/**
 * Handles the OCPP 1.6 "MeterValues" action (§4.4).
 * <p>
 * Extracts the latest sampled Energy.Active.Import.Register value from the
 * meterValue array and publishes a {@link MeterValuesReceivedEvent} for the
 * Charging module to process.
 * <p>
 * Per OCPP 1.6, MeterValues.conf is an empty payload ({}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MeterValuesHandler implements OcppActionHandler {

    private static final String ACTION = "MeterValues";
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public OcppCallResult handle(String chargePointId, OcppCall call) {
        JsonNode payload = call.payload();

        int connectorId = payload.path("connectorId").asInt(0);
        Integer transactionId = payload.has("transactionId") && !payload.path("transactionId").isNull()
                ? payload.path("transactionId").asInt()
                : null;

        // Extract the latest meter value from the meterValue array
        // Per OCPP 1.6 §4.4, meterValue is an array of MeterValue objects,
        // each containing a timestamp and sampledValue array.
        // We look for the Energy.Active.Import.Register measurand.
        Integer meterValue = null;
        LocalDateTime timestamp = LocalDateTime.now();
        JsonNode meterValueArray = payload.path("meterValue");

        if (meterValueArray.isArray() && !meterValueArray.isEmpty()) {
            // Take the last (most recent) MeterValue entry
            JsonNode lastMeterValue = meterValueArray.get(meterValueArray.size() - 1);

            // Parse timestamp
            String timestampStr = lastMeterValue.path("timestamp").asText(null);
            if (timestampStr != null) {
                try {
                    timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    log.warn("Failed to parse MeterValues timestamp: {}", timestampStr);
                }
            }

            // Find Energy.Active.Import.Register in sampledValue array
            JsonNode sampledValues = lastMeterValue.path("sampledValue");
            if (sampledValues.isArray()) {
                for (JsonNode sample : sampledValues) {
                    String measurand = sample.path("measurand").asText("Energy.Active.Import.Register");
                    if ("Energy.Active.Import.Register".equals(measurand)) {
                        String valueStr = sample.path("value").asText("0");
                        try {
                            meterValue = (int) Double.parseDouble(valueStr);
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse MeterValues value: {}", valueStr);
                        }
                        break;
                    }
                }
            }
        }

        log.info("MeterValues from CP {}: connectorId={}, transactionId={}, meterValue={} Wh, timestamp={}",
                chargePointId, connectorId, transactionId, meterValue, timestamp);

        if (meterValue != null && transactionId != null) {
            eventPublisher.publishEvent(new MeterValuesReceivedEvent(
                    chargePointId, connectorId, transactionId, meterValue, timestamp));
        } else {
            log.debug("Skipping MeterValues event: meterValue={}, transactionId={}", meterValue, transactionId);
        }

        // MeterValues.conf is an empty payload per OCPP 1.6 §4.4
        ObjectNode confPayload = objectMapper.createObjectNode();
        return new OcppCallResult(call.messageId(), confPayload);
    }
}
