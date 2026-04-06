package com.project.evgo.payment.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.events.CableUnpluggedEvent;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.station.PriceSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

/**
 * Listens to {@link CableUnpluggedEvent} and calculates idle overstay fees.
 * <p>
 * Per OCPP 1.6 §5 status flow:
 * Charging → SuspendedEV/Finishing → Available (cable unplugged)
 * <p>
 * Idle time = Available timestamp (cableUnpluggedTime) - StopTransaction timestamp (idleStartTime)
 * <p>
 * Uses {@link PriceSettingService#calculateIdleFee(Long, int)} which applies:
 * max(0, overstayMinutes - gracePeriodMinutes) × idlePenaltyPerMinute
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdleFeeListener {

    private final InvoiceRepository invoiceRepository;
    private final ChargerService chargerService;
    private final PriceSettingService priceSettingService;

    @ApplicationModuleListener
    @Transactional
    public void onCableUnplugged(CableUnpluggedEvent event) {
        log.info("Received CableUnpluggedEvent: sessionId={}, portId={}, userId={}, " +
                        "idleStartTime={}, cableUnpluggedTime={}",
                event.sessionId(), event.portId(), event.userId(),
                event.idleStartTime(), event.cableUnpluggedTime());

        // Guard: if idleStartTime is null, session may not have been properly completed
        if (event.idleStartTime() == null || event.cableUnpluggedTime() == null) {
            log.warn("Cannot calculate idle fee: idleStartTime or cableUnpluggedTime is null for session={}",
                    event.sessionId());
            return;
        }

        // Calculate idle minutes
        Duration idleDuration = Duration.between(event.idleStartTime(), event.cableUnpluggedTime());
        int idleMinutes = (int) idleDuration.toMinutes();

        if (idleMinutes <= 0) {
            log.debug("No idle time for session={} ({}min). Skipping idle fee.", event.sessionId(), idleMinutes);
            return;
        }

        // Resolve portId → chargerId → stationId
        PortResponse port = chargerService.findPortById(event.portId()).orElse(null);
        if (port == null) {
            log.error("Cannot calculate idle fee: port not found for portId={}", event.portId());
            return;
        }

        ChargerResponse charger = chargerService.findById(port.getChargerId()).orElse(null);
        if (charger == null) {
            log.error("Cannot calculate idle fee: charger not found for chargerId={}", port.getChargerId());
            return;
        }

        Long stationId = charger.getStationId();

        // Calculate idle fee using station pricing
        BigDecimal idleFee;
        try {
            idleFee = priceSettingService.calculateIdleFee(stationId, idleMinutes);
        } catch (Exception e) {
            log.error("Cannot calculate idle fee for stationId={}: {}", stationId, e.getMessage());
            return;
        }

        if (idleFee.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Idle fee is zero for session={} ({}min idle, within grace period). No invoice created.",
                    event.sessionId(), idleMinutes);
            return;
        }

        // Create idle fee invoice
        Invoice invoice = new Invoice();
        invoice.setChargingSessionId(event.sessionId());
        invoice.setUserId(event.userId());
        invoice.setTotalCost(idleFee);
        invoice.setPurpose(InvoicePurpose.IDLE_FEE);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setNumber("INV-IDLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + event.sessionId());

        invoiceRepository.save(invoice);
        log.info("Created idle fee invoice for session={}: number={}, idleFee={} ({}min idle)",
                event.sessionId(), invoice.getNumber(), idleFee, idleMinutes);
    }
}
