package com.project.evgo.ocpp.internal.OcppActionHandlerImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartTransactionHandlerTest {

    @Mock
    private ChargerService chargerService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StartTransactionHandler handler;

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<StartTransactionReceivedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new StartTransactionHandler(objectMapper, chargerService, eventPublisher, jdbcTemplate);
    }

    @Test
    void getAction_shouldReturnStartTransaction() {
        assertThat(handler.getAction()).isEqualTo("StartTransaction");
    }

    @Test
    void handle_shouldReturnCallResultAndPublishEvent() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("idTag", "test_user");
        payload.put("meterStart", 500);
        payload.put("timestamp", "2023-10-27T10:00:00Z");

        OcppCall call = new OcppCall("msg-1", "StartTransaction", payload);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(42);

        PortResponse port = new PortResponse();
        port.setId(200L);
        port.setPortNumber(1);

        when(chargerService.findPortsByChargerId(100L)).thenReturn(List.of(port));

        // Act
        OcppCallResult result = handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        StartTransactionReceivedEvent event = eventCaptor.getValue();
        
        assertThat(event.chargePointId()).isEqualTo("100");
        assertThat(event.connectorId()).isEqualTo(1);
        assertThat(event.portId()).isEqualTo(200L);
        assertThat(event.transactionId()).isEqualTo(42);
        assertThat(event.idTag()).isEqualTo("test_user");
        assertThat(event.meterStart()).isEqualTo(500);
        assertThat(event.reservationId()).isNull();
        assertThat(event.timestamp()).isNotNull();

        assertThat(result.messageId()).isEqualTo("msg-1");
        
        ObjectNode resultPayload = (ObjectNode) result.payload();
        assertThat(resultPayload.get("transactionId").asInt()).isEqualTo(42);
        assertThat(resultPayload.get("idTagInfo").get("status").asText()).isEqualTo("Accepted");
    }

    @Test
    void handle_shouldHandleInvalidChargePointIdGracefully() {
        // Arrange
        String chargePointId = "invalid_id";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("idTag", "test_user");
        payload.put("meterStart", 500);
        payload.put("timestamp", "2023-10-27T10:00:00Z");

        OcppCall call = new OcppCall("msg-1", "StartTransaction", payload);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(42);

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(chargerService, never()).findPortsByChargerId(any());
        
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        StartTransactionReceivedEvent event = eventCaptor.getValue();
        
        assertThat(event.chargePointId()).isEqualTo("invalid_id");
        assertThat(event.portId()).isNull();
    }

    @Test
    void handle_shouldHandleReservationId() {
        // Arrange
        String chargePointId = "100";
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("connectorId", 1);
        payload.put("idTag", "test_user");
        payload.put("meterStart", 500);
        payload.put("timestamp", "2023-10-27T10:00:00Z");
        payload.put("reservationId", 99);

        OcppCall call = new OcppCall("msg-1", "StartTransaction", payload);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(42);

        PortResponse port = new PortResponse();
        port.setId(200L);
        port.setPortNumber(1);

        when(chargerService.findPortsByChargerId(100L)).thenReturn(List.of(port));

        // Act
        handler.handle(chargePointId, call);

        // Assert
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        StartTransactionReceivedEvent event = eventCaptor.getValue();
        
        assertThat(event.reservationId()).isEqualTo(99);
    }
}
