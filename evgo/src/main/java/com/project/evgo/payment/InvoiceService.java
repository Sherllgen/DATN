package com.project.evgo.payment;

import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;

public interface InvoiceService {
    InvoiceResponse findByBookingId(Long bookingId);

    InvoiceResponse findByChargingSessionId(Long chargingSessionId);

    void createInvoice(InvoiceCreatedRequest request);

    InvoiceResponse findById(Long invoiceId);

    PageResponse<InvoiceResponse> getMyInvoices(Long userId, InvoiceStatus status, int page, int size);

    boolean hasUnpaidInvoices(Long userId);
}
