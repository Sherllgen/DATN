package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.sharedkernel.enums.PortStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusNotificationHandlerTest {

    @Mock
    private ChargerService chargerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StatusNotificationHandler handler;

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<StatusNotificationReceivedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAction_shouldReturnStatusNotification() {
        assertThat(handler.getAction()).isEqualTo("StatusNotification");
    }

    @Test
    void handle_shouldUpdatePortStatusAndPublishEvent() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("status", "Charging");
        payload.put("errorCode", "NoError");
        payload.put("info", "Additional info");
        payload.put("timestamp", "2023-10-27T10:00:00Z");

        OcppCall call = new OcppCall("msg-1", "StatusNotification", payload);

        PortResponse port = new PortResponse();
        port.setId(200L);
        port.setPortNumber(1);

        when(chargerService.findPortsByChargerId(100L)).thenReturn(List.of(port));

        // Act
        OcppCallResult result = handler.handle(chargePointId, call);

        // Assert
        verify(chargerService).internalUpdatePortStatus(200L, PortStatus.CHARGING);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        StatusNotificationReceivedEvent event = eventCaptor.getValue();
        assertThat(event.chargePointId()).isEqualTo("100");
        assertThat(event.connectorId()).isEqualTo(1);
        assertThat(event.portId()).isEqualTo(200L);
        assertThat(event.status()).isEqualTo("Charging");
        assertThat(event.errorCode()).isEqualTo("NoError");
        assertThat(event.info()).isEqualTo("Additional info");
        assertThat(event.timestamp()).isNotNull();

        assertThat(result.messageId()).isEqualTo("msg-1");
        assertThat(result.payload().isEmpty()).isTrue();
    }

    @Test
    void handle_shouldHandleInvalidChargePointIdGracefully() {
        // Arrange
        String chargePointId = "invalid_id";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("status", "Available");
        payload.put("errorCode", "NoError");

        OcppCall call = new OcppCall("msg-2", "StatusNotification", payload);

        // Act
        OcppCallResult result = handler.handle(chargePointId, call);

        // Assert
        verify(chargerService, never()).findPortsByChargerId(any());
        verify(chargerService, never()).internalUpdatePortStatus(any(), any());
        
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        StatusNotificationReceivedEvent event = eventCaptor.getValue();
        assertThat(event.chargePointId()).isEqualTo("invalid_id");
        assertThat(event.portId()).isNull();

        assertThat(result.messageId()).isEqualTo("msg-2");
    }

    @Test
    void handle_shouldHandleUnknownStatus() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("status", "SomeUnknownStatus");

        OcppCall call = new OcppCall("msg-3", "StatusNotification", payload);

        PortResponse port = new PortResponse();
        port.setId(200L);
        port.setPortNumber(1);

        when(chargerService.findPortsByChargerId(100L)).thenReturn(List.of(port));

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(chargerService).internalUpdatePortStatus(200L, PortStatus.UNAVAILABLE);
    }

    @Test
    void handle_shouldHandleMissingTimestampAndFields() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        // Missing connectorId, status, errorCode -> defaults will be used

        OcppCall call = new OcppCall("msg-4", "StatusNotification", payload);

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        StatusNotificationReceivedEvent event = eventCaptor.getValue();
        assertThat(event.connectorId()).isEqualTo(0);
        assertThat(event.status()).isEqualTo("Unknown");
        assertThat(event.errorCode()).isEqualTo("NoError");
        assertThat(event.timestamp()).isNull();
    }
}
