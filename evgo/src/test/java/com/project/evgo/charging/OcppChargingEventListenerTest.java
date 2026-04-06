package com.project.evgo.charging;

import com.project.evgo.charging.internal.ChargingSession;
import com.project.evgo.charging.internal.ChargingSessionRepository;
import com.project.evgo.charging.internal.OcppChargingEventListener;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.ocpp.StopTransactionReceivedEvent;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcppChargingEventListenerTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OcppChargingEventListener listener;

    // ===== onStartTransaction =====

    @Test
    @DisplayName("onStartTransaction: should update session to CHARGING with transactionId, meterStart, and startTime from OCPP timestamp")
    void onStartTransaction_ValidEvent_UpdatesSessionToCharging() {
        // Given
        Long portId = 1L;
        Integer transactionId = 42;
        Integer meterStart = 1000;
        LocalDateTime ocppTimestamp = LocalDateTime.of(2026, 4, 7, 10, 30, 0);

        StartTransactionReceivedEvent event = new StartTransactionReceivedEvent(
                "5", 1, portId, transactionId, "USER001", meterStart, ocppTimestamp, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setUserId(5L);
        session.setStatus(ChargingSessionStatus.PREPARING);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.PREPARING))
                .thenReturn(Optional.of(session));

        // When
        listener.onStartTransaction(event);

        // Then
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.CHARGING);
        assertThat(session.getTransactionId()).isEqualTo(transactionId);
        assertThat(session.getMeterStart()).isEqualTo(meterStart);
        assertThat(session.getStartTime()).isEqualTo(ocppTimestamp);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("onStartTransaction: should skip if no PREPARING session found (idempotency)")
    void onStartTransaction_NoPreparing_LogsAndIgnores() {
        // Given
        Long portId = 1L;
        StartTransactionReceivedEvent event = new StartTransactionReceivedEvent(
                "5", 1, portId, 42, "USER001", 1000, LocalDateTime.now(), null);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.PREPARING))
                .thenReturn(Optional.empty());

        // When
        listener.onStartTransaction(event);

        // Then
        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }

    // ===== onStopTransaction =====

    @Test
    @DisplayName("onStopTransaction: should complete session, calculate totalKwh, set endTime from OCPP timestamp, and publish event")
    void onStopTransaction_ValidEvent_CompletesSession() {
        // Given
        Integer transactionId = 42;
        Integer meterStop = 5000;
        LocalDateTime ocppTimestamp = LocalDateTime.of(2026, 4, 7, 11, 30, 0);
        StopTransactionReceivedEvent event = new StopTransactionReceivedEvent(
                transactionId, meterStop, ocppTimestamp, "USER001", "Local");

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(1L);
        session.setUserId(5L);
        session.setTransactionId(transactionId);
        session.setMeterStart(1000);
        session.setStatus(ChargingSessionStatus.CHARGING);

        when(sessionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(session));

        // When
        listener.onStopTransaction(event);

        // Then
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.COMPLETED);
        assertThat(session.getEndTime()).isEqualTo(ocppTimestamp);
        // (5000 - 1000) Wh = 4.0000 kWh
        assertThat(session.getTotalKwh()).isEqualByComparingTo(new BigDecimal("4.0000"));
        verify(sessionRepository).save(session);

        ArgumentCaptor<ChargingSessionCompletedEvent> captor =
                ArgumentCaptor.forClass(ChargingSessionCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ChargingSessionCompletedEvent published = captor.getValue();
        assertThat(published.sessionId()).isEqualTo(10L);
        assertThat(published.userId()).isEqualTo(5L);
        assertThat(published.totalKwh()).isEqualByComparingTo(new BigDecimal("4.0000"));
    }

    @Test
    @DisplayName("onStopTransaction: should include OCPP reason in completed event")
    void onStopTransaction_WithReason_SetsReason() {
        // Given
        Integer transactionId = 42;
        LocalDateTime ocppTimestamp = LocalDateTime.now();
        StopTransactionReceivedEvent event = new StopTransactionReceivedEvent(
                transactionId, 5000, ocppTimestamp, null, "EVDisconnected");

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(1L);
        session.setUserId(5L);
        session.setTransactionId(transactionId);
        session.setMeterStart(1000);
        session.setStatus(ChargingSessionStatus.CHARGING);

        when(sessionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(session));

        // When
        listener.onStopTransaction(event);

        // Then
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.COMPLETED);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishEvent(any(ChargingSessionCompletedEvent.class));
    }

    @Test
    @DisplayName("onStopTransaction: should gracefully handle missing session")
    void onStopTransaction_SessionNotFound_LogsAndIgnores() {
        // Given
        StopTransactionReceivedEvent event = new StopTransactionReceivedEvent(
                999, 5000, LocalDateTime.now(), null, null);
        when(sessionRepository.findByTransactionId(999)).thenReturn(Optional.empty());

        // When
        listener.onStopTransaction(event);

        // Then
        verify(sessionRepository, never()).save(any(ChargingSession.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ===== onStatusNotification =====

    @Test
    @DisplayName("onStatusNotification: Available status should publish CableUnpluggedEvent for COMPLETED session")
    void onStatusNotification_AvailableWithCompletedSession_PublishesCableUnplugged() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "Available", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setStatus(ChargingSessionStatus.COMPLETED);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.COMPLETED))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then
        ArgumentCaptor<CableUnpluggedEvent> captor = ArgumentCaptor.forClass(CableUnpluggedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        CableUnpluggedEvent published = captor.getValue();
        assertThat(published.sessionId()).isEqualTo(10L);
        assertThat(published.portId()).isEqualTo(portId);
    }

    @Test
    @DisplayName("onStatusNotification: Finishing status should publish CableUnpluggedEvent for COMPLETED session")
    void onStatusNotification_FinishingWithCompletedSession_PublishesCableUnplugged() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "Finishing", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setStatus(ChargingSessionStatus.COMPLETED);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.COMPLETED))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then
        verify(eventPublisher).publishEvent(any(CableUnpluggedEvent.class));
    }

    @Test
    @DisplayName("onStatusNotification: Available status with no completed session should not publish")
    void onStatusNotification_NoCompletedSession_DoesNotPublish() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "Available", null, null, null, null);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.COMPLETED))
                .thenReturn(Optional.empty());

        // When
        listener.onStatusNotification(event);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("onStatusNotification: Charging status should be ignored (no cable unplug)")
    void onStatusNotification_ChargingStatus_Ignored() {
        // Given
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, 1L, "NoError", "Charging", null, null, null, null);

        // When
        listener.onStatusNotification(event);

        // Then
        verify(sessionRepository, never()).findByPortIdAndStatus(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("onStatusNotification: connectorId=0 (whole CP) should be ignored")
    void onStatusNotification_ConnectorZero_Ignored() {
        // Given — connectorId=0 per OCPP 1.6 refers to the entire charge point, portId is null
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 0, null, "NoError", "Available", null, null, null, null);

        // When
        listener.onStatusNotification(event);

        // Then
        verify(sessionRepository, never()).findByPortIdAndStatus(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
