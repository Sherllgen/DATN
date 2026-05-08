package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.ocpp.MeterValuesReceivedEvent;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeterValuesHandlerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MeterValuesHandler handler;

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<MeterValuesReceivedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new MeterValuesHandler(objectMapper, eventPublisher);
    }

    @Test
    void getAction_shouldReturnMeterValues() {
        assertThat(handler.getAction()).isEqualTo("MeterValues");
    }

    @Test
    void handle_shouldReturnCallResultAndPublishEvent() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("transactionId", 42);

        ArrayNode meterValueArray = payload.putArray("meterValue");
        
        // Add one old meter value
        ObjectNode meterValue1 = meterValueArray.addObject();
        meterValue1.put("timestamp", "2023-10-27T10:00:00Z");
        ArrayNode sampledValueArray1 = meterValue1.putArray("sampledValue");
        ObjectNode sample1 = sampledValueArray1.addObject();
        sample1.put("measurand", "Energy.Active.Import.Register");
        sample1.put("value", "100.5");

        // Add newest meter value
        ObjectNode meterValue2 = meterValueArray.addObject();
        meterValue2.put("timestamp", "2023-10-27T10:05:00Z");
        ArrayNode sampledValueArray2 = meterValue2.putArray("sampledValue");
        ObjectNode sample2 = sampledValueArray2.addObject();
        sample2.put("measurand", "Energy.Active.Import.Register");
        sample2.put("value", "105.0");

        OcppCall call = new OcppCall("msg-1", "MeterValues", payload);

        // Act
        OcppCallResult result = handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        MeterValuesReceivedEvent event = eventCaptor.getValue();
        
        assertThat(event.chargePointId()).isEqualTo("100");
        assertThat(event.connectorId()).isEqualTo(1);
        assertThat(event.transactionId()).isEqualTo(42);
        assertThat(event.meterValue()).isEqualTo(105);
        assertThat(event.timestamp()).isNotNull();

        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(result.payload().isEmpty()).isTrue();
    }

    @Test
    void handle_shouldSkipPublishingIfTransactionIdIsNull() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        // no transactionId
        
        ArrayNode meterValueArray = payload.putArray("meterValue");
        ObjectNode meterValue1 = meterValueArray.addObject();
        meterValue1.put("timestamp", "2023-10-27T10:00:00Z");
        ArrayNode sampledValueArray1 = meterValue1.putArray("sampledValue");
        ObjectNode sample1 = sampledValueArray1.addObject();
        sample1.put("measurand", "Energy.Active.Import.Register");
        sample1.put("value", "100.5");

        OcppCall call = new OcppCall("msg-1", "MeterValues", payload);

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void handle_shouldSkipPublishingIfMeterValueNotFound() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("transactionId", 42);
        
        ArrayNode meterValueArray = payload.putArray("meterValue");
        ObjectNode meterValue1 = meterValueArray.addObject();
        meterValue1.put("timestamp", "2023-10-27T10:00:00Z");
        ArrayNode sampledValueArray1 = meterValue1.putArray("sampledValue");
        ObjectNode sample1 = sampledValueArray1.addObject();
        sample1.put("measurand", "Voltage");
        sample1.put("value", "220.0");

        OcppCall call = new OcppCall("msg-1", "MeterValues", payload);

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher, never()).publishEvent(any());
    }
}
