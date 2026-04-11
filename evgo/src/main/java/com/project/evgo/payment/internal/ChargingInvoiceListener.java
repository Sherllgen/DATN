package com.project.evgo.payment.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.response.PriceSettingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Listens to {@link ChargingSessionCompletedEvent} and creates a charging invoice.
 * <p>
 * Resolves the station's active pricing via the chain:
 * portId → PortResponse.chargerId → ChargerResponse.stationId → PriceSettingService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChargingInvoiceListener {

    private final InvoiceRepository invoiceRepository;
    private final ChargerService chargerService;
    private final PriceSettingService priceSettingService;

    /**
     * Creates a PENDING invoice for a completed charging session.
     * <p>
     * totalCost = totalKwh × chargingRatePerKwh (from the station's active price setting)
     */
    @ApplicationModuleListener
    public void onChargingSessionCompleted(ChargingSessionCompletedEvent event) {
        log.info("Received ChargingSessionCompletedEvent: sessionId={}, userId={}, portId={}, totalKwh={}",
                event.sessionId(), event.userId(), event.portId(), event.totalKwh());

        // Check if invoice already exists for this session (idempotency)
        if (invoiceRepository.findByChargingSessionId(event.sessionId()).isPresent()) {
            log.warn("Invoice already exists for chargingSessionId={}. Skipping.", event.sessionId());
            return;
        }

        // Resolve portId → chargerId → stationId
        PortResponse port = chargerService.findPortById(event.portId()).orElse(null);
        if (port == null) {
            log.error("Cannot create charging invoice: port not found for portId={}", event.portId());
            return;
        }

        ChargerResponse charger = chargerService.findById(port.getChargerId()).orElse(null);
        if (charger == null) {
            log.error("Cannot create charging invoice: charger not found for chargerId={}", port.getChargerId());
            return;
        }

        Long stationId = charger.getStationId();

        // Get active pricing
        PriceSettingResponse pricing;
        try {
            pricing = priceSettingService.getActivePriceSetting(stationId);
        } catch (Exception e) {
            log.error("Cannot create charging invoice: no active pricing for stationId={}", stationId, e);
            return;
        }

        BigDecimal chargingRatePerKwh = pricing.chargingRatePerKwh();
        BigDecimal totalCost = event.totalKwh().multiply(chargingRatePerKwh)
                .setScale(2, RoundingMode.HALF_UP);

        Invoice invoice = new Invoice();
        invoice.setChargingSessionId(event.sessionId());
        invoice.setUserId(event.userId());
        invoice.setTotalCost(totalCost);
        invoice.setPurpose(InvoicePurpose.CHARGING_SESSION);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setNumber("INV-CHG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + event.sessionId());

        invoiceRepository.save(invoice);
        log.info("Created charging invoice for session={}: number={}, totalCost={} ({}kWh × {}VND/kWh)",
                event.sessionId(), invoice.getNumber(), totalCost,
                event.totalKwh(), chargingRatePerKwh);
    }
}
