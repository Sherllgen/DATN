package com.project.evgo.payment.internal;

import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.events.RequireRefundEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for events requiring a refund and processes them.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequireRefundListener {

    private final InvoiceRepository invoiceRepository;

    @EventListener
    @Transactional
    public void onRequireRefund(RequireRefundEvent event) {
        log.info("Received RequireRefundEvent for bookingId: {}, amount: {}, reason: {}",
                event.bookingId(), event.amount(), event.reason());

        invoiceRepository.findByBookingId(event.bookingId()).ifPresentOrElse(invoice -> {
            log.info("Initiating refund process for invoiceId: {}", invoice.getId());
            // TODO: Implement actual gateway refund logic (e.g. ZaloPay refund API)
            
            // Mark the invoice as REFUNDED for now as a mock operation
            invoice.setStatus(InvoiceStatus.REFUNDED);
            invoiceRepository.save(invoice);
            log.info("Invoice {} marked as REFUNDED", invoice.getId());
        }, () -> {
            log.warn("No invoice found for bookingId: {} to process refund", event.bookingId());
        });
    }
}
