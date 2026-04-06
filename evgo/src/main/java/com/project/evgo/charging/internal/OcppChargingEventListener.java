package com.project.evgo.charging.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.project.evgo.sharedkernel.events.CableUnpluggedEvent;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;
import com.project.evgo.ocpp.StartTransactionReceivedEvent;
import com.project.evgo.ocpp.StatusNotificationReceivedEvent;
import com.project.evgo.ocpp.StopTransactionReceivedEvent;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;

/**
 * Listens to OCPP-originated Application Events and updates ChargingSession state accordingly.
 * <p>
 * Uses {@code @ApplicationModuleListener} to guarantee correct transaction/async boundaries
 * as recommended by Spring Modulith.
 * <p>
 * OCPP 1.6J Reference: {@code doc/ocpp.md/docs/OCPP-1.6J.md}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcppChargingEventListener {

    private final ChargingSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Handles StartTransaction from the charge point.
     * <p>
     * Per OCPP 1.6 §4.10, StartTransaction.req contains: connectorId, idTag, meterStart, timestamp.
     * The Central System responds with a transactionId in StartTransaction.conf.
     * <p>
     * Transitions the PREPARING session to CHARGING, records meterStart, transactionId, and startTime
     * using the OCPP-provided timestamp.
     * <p>
     * Idempotency: if no PREPARING session found (e.g. already CHARGING), logs and ignores.
     */
    @ApplicationModuleListener
    @Transactional
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
    }

    /**
     * Handles StopTransaction from the charge point.
     * <p>
     * Per OCPP 1.6 §4.12, StopTransaction.req contains: transactionId, meterStop, timestamp,
     * and optionally idTag, reason, and transactionData (meter values).
     * <p>
     * Valid reasons per spec: EmergencyStop, EVDisconnected, HardReset, Local, Other,
     * PowerLoss, Reboot, Remote, SoftReset, UnlockCommand, DeAuthorized.
     * <p>
     * Completes the session, calculates totalKwh (meterStop - meterStart in Wh → kWh),
     * sets endTime from the OCPP timestamp, and publishes {@link ChargingSessionCompletedEvent}.
     */
    @ApplicationModuleListener
    @Transactional
    public void onStopTransaction(StopTransactionReceivedEvent event) {
        log.info("Received StopTransactionReceivedEvent: transactionId={}, meterStop={}, timestamp={}, reason={}",
                event.transactionId(), event.meterStop(), event.timestamp(), event.reason());

        Optional<ChargingSession> optionalSession = sessionRepository.findByTransactionId(event.transactionId());

        if (optionalSession.isEmpty()) {
            log.warn("No session found for transactionId={}. Ignoring StopTransaction.", event.transactionId());
            return;
        }

        ChargingSession session = optionalSession.get();
        session.setStatus(ChargingSessionStatus.COMPLETED);
        session.setEndTime(event.timestamp());

        // Calculate totalKwh: (meterStop - meterStart) Wh → kWh
        Integer meterStart = session.getMeterStart() != null ? session.getMeterStart() : 0;
        BigDecimal energyWh = BigDecimal.valueOf(event.meterStop() - meterStart);
        BigDecimal totalKwh = energyWh.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        session.setTotalKwh(totalKwh);

        sessionRepository.save(session);
        log.info("Session {} COMPLETED: totalKwh={}, reason={}", session.getId(), totalKwh, event.reason());

        eventPublisher.publishEvent(new ChargingSessionCompletedEvent(
                session.getId(), session.getUserId(), session.getPortId(), totalKwh));
    }

    /**
     * Handles StatusNotification from the charge point.
     * <p>
     * Per OCPP 1.6 §4.11, StatusNotification.req contains: connectorId, errorCode, status,
     * and optionally info, timestamp, vendorErrorCode, vendorId.
     * <p>
     * connectorId=0 refers to the entire charge point (not a specific connector) per §2.2.
     * <p>
     * If status is "Available" or "Finishing" (indicating cable is unplugged or transaction ended),
     * looks for a COMPLETED session on the port and publishes {@link CableUnpluggedEvent}
     * to stop idle fee calculation.
     * <p>
     * Per OCPP 1.6 §5, typical status flow:
     * Available → Preparing → Charging → Finishing → Available
     */
    @ApplicationModuleListener
    @Transactional
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

        if (!"Available".equals(event.status()) && !"Finishing".equals(event.status())) {
            log.debug("StatusNotification status '{}' does not indicate cable unplug. Ignoring.", event.status());
            return;
        }

        if (event.portId() == null) {
            log.warn("StatusNotification has null portId. Cannot look up session. Ignoring.");
            return;
        }

        Optional<ChargingSession> optionalSession = sessionRepository.findByPortIdAndStatus(
                event.portId(), ChargingSessionStatus.COMPLETED);

        if (optionalSession.isEmpty()) {
            log.debug("No COMPLETED session found for portId={} on status '{}'. Nothing to do.",
                    event.portId(), event.status());
            return;
        }

        ChargingSession session = optionalSession.get();
        session.setStatus(ChargingSessionStatus.FINISHING);
        sessionRepository.save(session);

        // idleStartTime = session's endTime (when StopTransaction was received)
        // cableUnpluggedTime = StatusNotification timestamp, or now() if not provided
        java.time.LocalDateTime cableUnpluggedTime = event.timestamp() != null
                ? event.timestamp() : java.time.LocalDateTime.now();

        eventPublisher.publishEvent(new CableUnpluggedEvent(
                session.getId(), session.getPortId(), session.getUserId(),
                session.getEndTime(), cableUnpluggedTime));
        log.info("Published CableUnpluggedEvent for session={}, portId={}, idleStartTime={}, cableUnpluggedTime={}",
                session.getId(), session.getPortId(), session.getEndTime(), cableUnpluggedTime);
    }
}
