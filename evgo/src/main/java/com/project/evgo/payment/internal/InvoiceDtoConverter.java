package com.project.evgo.payment.internal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.project.evgo.payment.response.InvoiceResponse;

@Component
public class InvoiceDtoConverter {

    public InvoiceResponse convert(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .bookingId(invoice.getBookingId())
                .chargingSessionId(invoice.getChargingSessionId())
                .userId(invoice.getUserId())
                .number(invoice.getNumber())
                .totalCost(invoice.getTotalCost())
                .purpose(invoice.getPurpose())
                .status(invoice.getStatus())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    public List<InvoiceResponse> convert(List<Invoice> invoices) {
        return invoices.stream().map(this::convert).toList();
    }

    public Optional<InvoiceResponse> convert(Optional<Invoice> invoice) {
        return invoice.map(this::convert);
    }
}
