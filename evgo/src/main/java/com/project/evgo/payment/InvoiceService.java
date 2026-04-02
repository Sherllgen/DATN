package com.project.evgo.payment;

import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse findByBookingId(Long bookingId);

    void createInvoice(InvoiceCreatedRequest request);
}
