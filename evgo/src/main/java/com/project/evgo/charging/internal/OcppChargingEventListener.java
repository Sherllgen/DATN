package com.project.evgo.charging.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.project.evgo.sharedkernel.events.CableUnpluggedEvent;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;
import com.project.evgo.ocpp.MeterValuesReceivedEvent;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.ocpp.StopTransactionReceivedEvent;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;

/**
 * Listens to OCPP-originated Application Events and updates ChargingSession state accordingly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcppChargingEventListener {

    private static final String REDIS_METER_KEY_PREFIX = "evgo:charging:meter:";
    private static final Duration REDIS_METER_TTL = Duration.ofHours(24);

    private final ChargingSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final ChargingMonitorService chargingMonitorService;

    /**
     * Handles StartTransaction from the charge point.
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStartTransaction(StartTransactionReceivedEvent event) {
        log.info("Received StartTransactionReceivedEvent: chargePointId={}, connectorId={}, portId={}, " +
                        "transactionId={}, idTag={}, meterStart={}, timestamp={}",
                event.chargePointId(), event.connectorId(), event.portId(),
                event.transactionId(), event.idTag(), event.meterStart(), event.timestamp());

        Optional<ChargingSession> optionalSession = sessionRepository.findByPortIdAndStatus(
                event.portId(), ChargingSessionStatus.PREPARING);

        if (optionalSession.isEmpty()) {
            log.warn("No PREPARING session found for portId={}. " +
                    "Possibly already CHARGING or no session exists. Ignoring.", event.portId());
            return;
        }

        ChargingSession session = optionalSession.get();
        session.setStatus(ChargingSessionStatus.CHARGING);
        session.setTransactionId(event.transactionId());
        session.setMeterStart(event.meterStart());
        session.setStartTime(event.timestamp());

        sessionRepository.save(session);
        log.info("Session {} updated to CHARGING: transactionId={}, meterStart={}, startTime={}",
                session.getId(), event.transactionId(), event.meterStart(), event.timestamp());
        chargingMonitorService.pushUpdate(session, event.meterStart(), event.timestamp());
    }

    /**
     * Handles StopTransaction from the charge point.
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStopTransaction(StopTransactionReceivedEvent event) {
        log.info("Received StopTransactionReceivedEvent: transactionId={}, meterStop={}, timestamp={}, reason={}",
                event.transactionId(), event.meterStop(), event.timestamp(), event.reason());

        // Look for the active session with this transactionId (CHARGING or SUSPENDED)
        List<ChargingSessionStatus> activeStatuses = List.of(
                ChargingSessionStatus.CHARGING,
                ChargingSessionStatus.SUSPENDED_EV,
                ChargingSessionStatus.SUSPENDED_EVSE);
        Optional<ChargingSession> optionalSession = sessionRepository.findByTransactionIdAndStatusIn(
                event.transactionId(), activeStatuses);

        if (optionalSession.isEmpty()) {
            log.warn("No session found for transactionId={}. Ignoring StopTransaction.", event.transactionId());
            return;
        }

        ChargingSession session = optionalSession.get();
        session.setStatus(ChargingSessionStatus.FINISHING);
        session.setEndTime(event.timestamp());

        // Calculate totalKwh: (meterStop - meterStart) Wh → kWh
        Integer meterStart = session.getMeterStart() != null ? session.getMeterStart() : 0;
        
        BigDecimal energyWh;
        if (event.meterStop() < meterStart) {
            log.warn("meterStop ({} Wh) is less than meterStart ({} Wh) for session {}. Assuming session-relative measurement.", 
                    event.meterStop(), meterStart, session.getId());
            energyWh = BigDecimal.valueOf(event.meterStop());
        } else {
            energyWh = BigDecimal.valueOf(event.meterStop() - meterStart);
        }
        
        BigDecimal totalKwh = energyWh.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        session.setTotalKwh(totalKwh);

        sessionRepository.save(session);
        log.info("Session {} set to FINISHING: totalKwh={}, reason={}", session.getId(), totalKwh, event.reason());
        chargingMonitorService.pushUpdate(session, null, event.timestamp());

        eventPublisher.publishEvent(new ChargingSessionCompletedEvent(
                session.getId(), session.getUserId(), session.getPortId(), totalKwh, event.reason(), session.getBookingId()));
    }

    /**
     * Handles StatusNotification from the charge point.
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStatusNotification(StatusNotificationReceivedEvent event) {
        log.info("Received StatusNotificationReceivedEvent: chargePointId={}, connectorId={}, portId={}, " +
                        "status={}, errorCode={}",
                event.chargePointId(), event.connectorId(), event.portId(),
                event.status(), event.errorCode());

        // connectorId=0 refers to the entire charge point, not a specific connector
        if (event.connectorId() != null && event.connectorId() == 0) {
            log.debug("StatusNotification for connectorId=0 (whole charge point). Ignoring for session tracking.");
            return;
        }

        if (event.portId() == null) {
            log.warn("StatusNotification has null portId. Cannot look up session. Ignoring.");
            return;
        }

        String status = event.status();

        // Handle SuspendedEV / SuspendedEVSE — update CHARGING or already-suspended session.
        if ("SuspendedEV".equals(status) || "SuspendedEVSE".equals(status)) {
            List<ChargingSessionStatus> activeStatuses = List.of(
                    ChargingSessionStatus.CHARGING,
                    ChargingSessionStatus.SUSPENDED_EV,
                    ChargingSessionStatus.SUSPENDED_EVSE);
            Optional<ChargingSession> optionalSession = sessionRepository.findFirstByPortIdAndStatusIn(
                    event.portId(), activeStatuses);
            if (optionalSession.isPresent()) {
                ChargingSession session = optionalSession.get();
                ChargingSessionStatus newStatus = "SuspendedEV".equals(status)
                        ? ChargingSessionStatus.SUSPENDED_EV
                        : ChargingSessionStatus.SUSPENDED_EVSE;
                session.setStatus(newStatus);
                sessionRepository.save(session);
                log.info("Session {} updated to {} from StatusNotification.", session.getId(), newStatus);
                String redisKey = REDIS_METER_KEY_PREFIX + session.getId();
                String cachedMeterStr = redisTemplate.opsForValue().get(redisKey);
                Integer currentMeter = null;
                if (cachedMeterStr != null) {
                    try {
                        currentMeter = Integer.parseInt(cachedMeterStr);
                    } catch (NumberFormatException ignored) {}
                }
                chargingMonitorService.pushUpdate(session, currentMeter, event.timestamp());
            } else {
                log.debug("No active session (CHARGING/SUSPENDED_EV/SUSPENDED_EVSE) found for portId={} on status '{}'. Nothing to do.",
                        event.portId(), status);
            }
            return;
        }

        // Handle Available — cable unplugged, mark FINISHING session as COMPLETED
        if (!"Available".equals(status)) {
            log.debug("StatusNotification status '{}' is not handled for session tracking. Ignoring.", status);
            return;
        }

        Optional<ChargingSession> optionalSession = sessionRepository.findByPortIdAndStatus(
                event.portId(), ChargingSessionStatus.FINISHING);

        if (optionalSession.isEmpty()) {
            log.debug("No FINISHING session found for portId={} on status '{}'. Nothing to do.",
                    event.portId(), status);
            return;
        }

        ChargingSession session = optionalSession.get();
        session.setStatus(ChargingSessionStatus.COMPLETED);
        sessionRepository.save(session);

        // idleStartTime = session's endTime (when StopTransaction was received)
        // cableUnpluggedTime = StatusNotification timestamp, or now() if not provided
        LocalDateTime cableUnpluggedTime = event.timestamp() != null
                ? event.timestamp() : LocalDateTime.now();
        chargingMonitorService.pushUpdate(session, null, cableUnpluggedTime);

        eventPublisher.publishEvent(new CableUnpluggedEvent(
                session.getId(), session.getPortId(), session.getUserId(),
                session.getEndTime(), cableUnpluggedTime));
        log.info("Session {} COMPLETED. Published CableUnpluggedEvent: idleStartTime={}, cableUnpluggedTime={}",
                session.getId(), session.getEndTime(), cableUnpluggedTime);
    }

    /**
     * Handles MeterValues from the charge point.
     */
    @EventListener
    public void onMeterValues(MeterValuesReceivedEvent event) {
        log.info("Received MeterValuesReceivedEvent: transactionId={}, meterValue={} Wh, timestamp={}",
                event.transactionId(), event.meterValue(), event.timestamp());

        if (event.transactionId() == null) {
            log.debug("MeterValues event has no transactionId. Ignoring.");
            return;
        }

        // Look for the active session with this transactionId.
        Optional<ChargingSession> optionalSession = sessionRepository.findFirstByTransactionIdOrderByCreatedAtDesc(
                event.transactionId());
        if (optionalSession.isEmpty()) {
            log.warn("No session found for transactionId={} on MeterValues. Ignoring.", event.transactionId());
            return;
        }

        ChargingSession session = optionalSession.get();
        Long sessionId = session.getId();

        // Cache current meter value in Redis with 24h TTL
        String redisKey = REDIS_METER_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(event.meterValue()), REDIS_METER_TTL);
        log.debug("Cached meterValue={} Wh for sessionId={} in Redis key={}",
                event.meterValue(), sessionId, redisKey);

        // Push SSE update to subscribed client
        chargingMonitorService.pushUpdate(session, event.meterValue(), event.timestamp());
    }
}
