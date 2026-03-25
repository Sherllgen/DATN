package com.project.evgo.payment.internal;

import com.project.evgo.booking.BookingCreatedEvent;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentModuleListener {

    private final InvoiceRepository invoiceRepository;

    @EventListener
    @Transactional
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("Payment module received BookingCreatedEvent for booking {}", event.bookingId());

        Invoice invoice = new Invoice();
        invoice.setBookingId(event.bookingId());
        invoice.setUserId(event.userId());
        invoice.setTotalCost(event.totalPrice());
        invoice.setPurpose(InvoicePurpose.BOOKING);
        invoice.setStatus(InvoiceStatus.PENDING);
        // Generate a unique invoice number
        invoice.setNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + event.bookingId());

        invoiceRepository.save(invoice);
        log.info("Invoice created for booking {}: {}", event.bookingId(), invoice.getNumber());
    }
}
