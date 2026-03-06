package com.project.evgo.payment.internal;

import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.user.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Data initializer for Invoice entities.
 * Seeds test invoices for payment flow testing (ZaloPay, etc.)
 * Runs after UserDataInitializer (Order 1) and StationDataInitializer (Order
 * 2).
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class InvoiceDataInitializer implements CommandLineRunner {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (invoiceRepository.count() > 0) {
            log.info("Invoices already initialized. Skipping seed data.");
            return;
        }

        log.info("Initializing invoice seed data...");

        try {
            Long userId = userRepository.findByEmail("user@gmail.com")
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Customer user not found. Ensure UserDataInitializer runs first."))
                    .getId();

            // Invoice 1 — PENDING, ready to pay via ZaloPay
            Invoice inv1 = buildInvoice(
                    "INV-2026-0001",
                    userId,
                    null, // no booking
                    1L, // chargingSessionId (dummy)
                    new BigDecimal("85000"),
                    InvoicePurpose.CHARGING_SESSION,
                    InvoiceStatus.PENDING);

            // Invoice 2 — PENDING booking invoice
            Invoice inv2 = buildInvoice(
                    "INV-2026-0002",
                    userId,
                    1L, // bookingId (dummy)
                    null,
                    new BigDecimal("150000"),
                    InvoicePurpose.BOOKING,
                    InvoiceStatus.PENDING);

            // Invoice 3 — PENDING, larger amount
            Invoice inv3 = buildInvoice(
                    "INV-2026-0003",
                    userId,
                    null,
                    2L,
                    new BigDecimal("320000"),
                    InvoicePurpose.CHARGING_SESSION,
                    InvoiceStatus.PENDING);

            // Invoice 4 — Already PAID (reference only, cannot be paid again)
            Invoice inv4 = buildInvoice(
                    "INV-2026-0004",
                    userId,
                    2L,
                    null,
                    new BigDecimal("200000"),
                    InvoicePurpose.BOOKING,
                    InvoiceStatus.PAID);

            // Invoice 5 — CANCELLED
            Invoice inv5 = buildInvoice(
                    "INV-2026-0005",
                    userId,
                    null,
                    3L,
                    new BigDecimal("50000"),
                    InvoicePurpose.CHARGING_SESSION,
                    InvoiceStatus.CANCELLED);

            invoiceRepository.save(inv1);
            invoiceRepository.save(inv2);
            invoiceRepository.save(inv3);
            invoiceRepository.save(inv4);
            invoiceRepository.save(inv5);

            log.info("Invoice seed data initialized successfully! Created 5 invoices for userId={}", userId);
            log.info(
                    "  PENDING invoices (use for ZaloPay testing): INV-2026-0001 (id=1, 85,000đ), INV-2026-0002 (id=2, 150,000đ), INV-2026-0003 (id=3, 320,000đ)");
            log.info("  PAID invoice (already paid): INV-2026-0004 (id=4)");
            log.info("  CANCELLED invoice: INV-2026-0005 (id=5)");

        } catch (Exception e) {
            log.error("Failed to initialize invoice seed data", e);
        }
    }

    private Invoice buildInvoice(String number, Long userId,
            Long bookingId, Long chargingSessionId,
            BigDecimal totalCost, InvoicePurpose purpose,
            InvoiceStatus status) {
        Invoice invoice = new Invoice();
        invoice.setNumber(number);
        invoice.setUserId(userId);
        invoice.setBookingId(bookingId);
        invoice.setChargingSessionId(chargingSessionId);
        invoice.setTotalCost(totalCost);
        invoice.setPurpose(purpose);
        invoice.setStatus(status);
        return invoice;
    }
}
