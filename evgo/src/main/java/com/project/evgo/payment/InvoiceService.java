package com.project.evgo.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;

public interface InvoiceService {
    InvoiceResponse findByBookingId(Long bookingId);

    InvoiceResponse findByChargingSessionId(Long chargingSessionId);

    void createInvoice(InvoiceCreatedRequest request);

    InvoiceResponse findById(Long invoiceId);


    PageResponse<InvoiceResponse> getMyInvoices(Long userId, InvoiceStatus status, InvoicePurpose purpose, int page, int size);

    boolean hasUnpaidInvoices(Long userId);

    void cancelInvoiceByBookingId(Long bookingId);

    /**
     * Returns all PENDING invoices created before the given threshold.
     * Used by the ZaloPay fallback polling job.
     */
    List<InvoiceResponse> findPendingOlderThan(LocalDateTime threshold);

    String getLatestAppTransId(Long invoiceId);

    InvoiceStatsResponse getStatsByBookingIds(List<Long> bookingIds);

    Map<Integer, BigDecimal> getMonthlyRevenueByBookingIds(List<Long> bookingIds, int year);
}
