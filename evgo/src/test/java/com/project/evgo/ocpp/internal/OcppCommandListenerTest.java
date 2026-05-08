package com.project.evgo.ocpp.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.sharedkernel.events.SendRemoteStartCommandEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStopCommandEvent;
import com.project.evgo.sharedkernel.events.SendReserveNowCommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcppCommandListenerTest {

    @Mock
    private OcppSessionManager sessionManager;

    @Mock
    private PendingCommandManager pendingCommandManager;

    private ObjectMapper objectMapper;

    @InjectMocks
    private OcppCommandListener listener;

    @Mock
    private WebSocketSession webSocketSession;

    @Captor
    private ArgumentCaptor<TextMessage> textMessageCaptor;

    private final String chargePointId = "100";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new OcppCommandListener(sessionManager, objectMapper, pendingCommandManager);
    }

    @Test
    void onRemoteStop_shouldSendRemoteStopTransaction() throws IOException {
        SendRemoteStopCommandEvent event = new SendRemoteStopCommandEvent(1L, chargePointId, 123, "Test Stop");
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        listener.onRemoteStop(event);

        verify(webSocketSession).sendMessage(textMessageCaptor.capture());
        String payload = textMessageCaptor.getValue().getPayload();
        
        assertThat(payload).contains("RemoteStopTransaction");
        assertThat(payload).contains("\"transactionId\":123");
    }

    @Test
    void onRemoteStop_shouldSkipIfTransactionIdIsNull() throws IOException {
        SendRemoteStopCommandEvent event = new SendRemoteStopCommandEvent(1L, chargePointId, null, "Null transaction ID");
        
        listener.onRemoteStop(event);

        verify(sessionManager, never()).getSession(any());
    }

    @Test
    void onRemoteStop_shouldSkipIfSessionIsClosed() throws IOException {
        SendRemoteStopCommandEvent event = new SendRemoteStopCommandEvent(1L, chargePointId, 123, "Test Stop");
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(false);

        listener.onRemoteStop(event);

        verify(webSocketSession, never()).sendMessage(any());
    }

    @Test
    void onReserveNow_shouldSendReserveNowCommand() throws IOException {
        LocalDateTime expiry = LocalDateTime.of(2025, 1, 1, 12, 0);
        SendReserveNowCommandEvent event = new SendReserveNowCommandEvent(chargePointId, 1, "userId", expiry, 12345);
        
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        listener.onReserveNow(event);

        verify(pendingCommandManager).track(any(String.class), eq("ReserveNow"), eq(chargePointId), eq(1));
        verify(webSocketSession).sendMessage(textMessageCaptor.capture());
        String payload = textMessageCaptor.getValue().getPayload();
        
        assertThat(payload).contains("ReserveNow");
        assertThat(payload).contains("\"connectorId\":1");
        assertThat(payload).contains("\"idTag\":\"userId\"");
        assertThat(payload).contains("\"reservationId\":12345");
        // We do not check exact expiryDate formatting as it depends on system timezone
    }

    @Test
    void onReserveNow_shouldSkipIfSessionIsClosed() throws IOException {
        LocalDateTime expiry = LocalDateTime.of(2025, 1, 1, 12, 0);
        SendReserveNowCommandEvent event = new SendReserveNowCommandEvent(chargePointId, 1, "userId", expiry, 12345);
        
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(false);

        listener.onReserveNow(event);

        verify(webSocketSession, never()).sendMessage(any());
        verify(pendingCommandManager, never()).track(any(), any(), any(), any());
    }

    @Test
    void onChargingRemoteStart_shouldSendRemoteStartTransaction() throws IOException {
        SendRemoteStartCommandEvent event = new SendRemoteStartCommandEvent(1L, chargePointId, 1, "userId");
        
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        listener.onChargingRemoteStart(event);

        verify(webSocketSession).sendMessage(textMessageCaptor.capture());
        String payload = textMessageCaptor.getValue().getPayload();
        
        assertThat(payload).contains("RemoteStartTransaction");
        assertThat(payload).contains("\"idTag\":\"userId\"");
        assertThat(payload).contains("\"connectorId\":1");
    }

    @Test
    void onChargingRemoteStart_withoutConnectorId() throws IOException {
        SendRemoteStartCommandEvent event = new SendRemoteStartCommandEvent(1L, chargePointId, null, "userId");
        
        when(sessionManager.getSession(chargePointId)).thenReturn(webSocketSession);
        when(webSocketSession.isOpen()).thenReturn(true);

        listener.onChargingRemoteStart(event);

        verify(webSocketSession).sendMessage(textMessageCaptor.capture());
        String payload = textMessageCaptor.getValue().getPayload();
        
        assertThat(payload).contains("RemoteStartTransaction");
        assertThat(payload).contains("\"idTag\":\"userId\"");
        assertThat(payload).doesNotContain("\"connectorId\"");
    }
}
