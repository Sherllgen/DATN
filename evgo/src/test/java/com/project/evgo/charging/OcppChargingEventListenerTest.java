package com.project.evgo.charging;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.evgo.charging.internal.ChargingMonitorService;
import com.project.evgo.charging.internal.ChargingSession;
import com.project.evgo.charging.internal.ChargingSessionRepository;
import com.project.evgo.charging.internal.OcppChargingEventListener;
import com.project.evgo.ocpp.MeterValuesReceivedEvent;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.ocpp.StopTransactionReceivedEvent;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import com.project.evgo.sharedkernel.events.CableUnpluggedEvent;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcppChargingEventListenerTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ChargingMonitorService chargingMonitorService;

    @InjectMocks
    private OcppChargingEventListener listener;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

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
    @DisplayName("onStopTransaction: should set session to FINISHING, calculate totalKwh, and publish event")
    void onStopTransaction_ValidEvent_SetsFinishing() {
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

        // Then — session should be FINISHING (not COMPLETED), cable may still be connected
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.FINISHING);
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
        assertThat(published.reason()).isEqualTo("Local");
    }

    @Test
    @DisplayName("onStopTransaction: should include OCPP reason and set FINISHING status")
    void onStopTransaction_WithReason_SetsFinishing() {
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
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.FINISHING);
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
    @DisplayName("onStatusNotification: Available status should set FINISHING session to COMPLETED and publish CableUnpluggedEvent")
    void onStatusNotification_AvailableWithFinishingSession_SetsCompleted() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "Available", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setUserId(5L);
        session.setEndTime(LocalDateTime.of(2026, 4, 7, 11, 30, 0));
        session.setStatus(ChargingSessionStatus.FINISHING);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.FINISHING))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then — session should now be COMPLETED (cable unplugged)
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.COMPLETED);
        verify(sessionRepository).save(session);

        ArgumentCaptor<CableUnpluggedEvent> captor = ArgumentCaptor.forClass(CableUnpluggedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        CableUnpluggedEvent published = captor.getValue();
        assertThat(published.sessionId()).isEqualTo(10L);
        assertThat(published.portId()).isEqualTo(portId);
        assertThat(published.userId()).isEqualTo(5L);
        assertThat(published.idleStartTime()).isEqualTo(session.getEndTime());
        assertThat(published.cableUnpluggedTime()).isNotNull();
    }

    @Test
    @DisplayName("onStatusNotification: SuspendedEV should set CHARGING session to SUSPENDED_EV")
    void onStatusNotification_SuspendedEV_UpdatesSession() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "SuspendedEV", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setStatus(ChargingSessionStatus.CHARGING);

        List<ChargingSessionStatus> activeStatuses = List.of(
                ChargingSessionStatus.CHARGING,
                ChargingSessionStatus.SUSPENDED_EV,
                ChargingSessionStatus.SUSPENDED_EVSE);
        when(sessionRepository.findFirstByPortIdAndStatusIn(portId, activeStatuses))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.SUSPENDED_EV);
        verify(sessionRepository).save(session);
        verify(eventPublisher, never()).publishEvent(any(CableUnpluggedEvent.class));
    }

    @Test
    @DisplayName("onStatusNotification: SuspendedEVSE should set CHARGING session to SUSPENDED_EVSE")
    void onStatusNotification_SuspendedEVSE_UpdatesSession() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "SuspendedEVSE", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setStatus(ChargingSessionStatus.CHARGING);

        List<ChargingSessionStatus> activeStatuses = List.of(
                ChargingSessionStatus.CHARGING,
                ChargingSessionStatus.SUSPENDED_EV,
                ChargingSessionStatus.SUSPENDED_EVSE);
        when(sessionRepository.findFirstByPortIdAndStatusIn(portId, activeStatuses))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.SUSPENDED_EVSE);
        verify(sessionRepository).save(session);
        verify(eventPublisher, never()).publishEvent(any(CableUnpluggedEvent.class));
    }

    @Test
    @DisplayName("onStatusNotification: Available status with no FINISHING session should not publish")
    void onStatusNotification_NoFinishingSession_DoesNotPublish() {
        // Given
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "Available", null, null, null, null);

        when(sessionRepository.findByPortIdAndStatus(portId, ChargingSessionStatus.FINISHING))
                .thenReturn(Optional.empty());

        // When
        listener.onStatusNotification(event);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("onStatusNotification: SuspendedEV should transition SUSPENDED_EVSE to SUSPENDED_EV (inter-suspend)")
    void onStatusNotification_SuspendedEV_FromSuspendedEVSE_Updates() {
        // Given — session is already SUSPENDED_EVSE, OCPP sends SuspendedEV
        Long portId = 1L;
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, portId, "NoError", "SuspendedEV", null, null, null, null);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(portId);
        session.setStatus(ChargingSessionStatus.SUSPENDED_EVSE);

        List<ChargingSessionStatus> activeStatuses = List.of(
                ChargingSessionStatus.CHARGING,
                ChargingSessionStatus.SUSPENDED_EV,
                ChargingSessionStatus.SUSPENDED_EVSE);
        when(sessionRepository.findFirstByPortIdAndStatusIn(portId, activeStatuses))
                .thenReturn(Optional.of(session));

        // When
        listener.onStatusNotification(event);

        // Then — session should now be SUSPENDED_EV
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.SUSPENDED_EV);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("onStatusNotification: Charging status should be ignored (no session tracking needed)")
    void onStatusNotification_ChargingStatus_Ignored() {
        // Given
        StatusNotificationReceivedEvent event = new StatusNotificationReceivedEvent(
                "5", 1, 1L, "NoError", "Charging", null, null, null, null);

        // When
        listener.onStatusNotification(event);

        // Then
        verify(sessionRepository, never()).findByPortIdAndStatus(any(), any());
        verify(sessionRepository, never()).findFirstByPortIdAndStatusIn(any(), anyList());
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

    // ===== onMeterValues =====

    @Test
    @DisplayName("onMeterValues: should cache meter value in Redis and trigger SSE push")
    void onMeterValues_ValidEvent_CachesAndPushes() {
        // Given
        Integer transactionId = 42;
        Integer meterValue = 3500;
        LocalDateTime timestamp = LocalDateTime.now();
        MeterValuesReceivedEvent event = new MeterValuesReceivedEvent(
                "5", 1, transactionId, meterValue, timestamp);

        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setPortId(1L);
        session.setTransactionId(transactionId);
        session.setStatus(ChargingSessionStatus.CHARGING);

        when(sessionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(session));

        // When
        listener.onMeterValues(event);

        // Then
        verify(valueOperations).set(
                "evgo:charging:meter:10",
                "3500",
                Duration.ofHours(24));
        verify(chargingMonitorService).pushUpdate(10L, 3500, timestamp);
    }

    @Test
    @DisplayName("onMeterValues: should ignore event with null transactionId")
    void onMeterValues_NullTransactionId_Ignored() {
        // Given
        MeterValuesReceivedEvent event = new MeterValuesReceivedEvent(
                "5", 1, null, 3500, LocalDateTime.now());

        // When
        listener.onMeterValues(event);

        // Then
        verifyNoInteractions(sessionRepository);
        verifyNoInteractions(chargingMonitorService);
    }

    @Test
    @DisplayName("onMeterValues: should ignore event when session not found")
    void onMeterValues_SessionNotFound_Ignored() {
        // Given
        MeterValuesReceivedEvent event = new MeterValuesReceivedEvent(
                "5", 1, 999, 3500, LocalDateTime.now());

        when(sessionRepository.findByTransactionId(999)).thenReturn(Optional.empty());

        // When
        listener.onMeterValues(event);

        // Then
        verify(sessionRepository).findByTransactionId(999);
        verifyNoInteractions(chargingMonitorService);
    }
}
