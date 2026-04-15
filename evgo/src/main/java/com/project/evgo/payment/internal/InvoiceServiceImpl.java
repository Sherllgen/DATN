package com.project.evgo.payment.internal;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDtoConverter invoiceDtoConverter;
    
    @Override
    public InvoiceResponse findByBookingId(Long bookingId) {
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceDtoConverter.convert(invoice);
    }

    @Override
    public InvoiceResponse findByChargingSessionId(Long chargingSessionId) {
        Invoice invoice = invoiceRepository.findByChargingSessionId(chargingSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceDtoConverter.convert(invoice);
    }

    @Override
    public void createInvoice(InvoiceCreatedRequest request) {
        if (invoiceRepository.findByBookingId(request.bookingId()).isPresent()) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXIST);
        }

        Invoice invoice = new Invoice();
        invoice.setBookingId(request.bookingId());
        invoice.setUserId(request.userId());
        invoice.setTotalCost(request.totalPrice());
        invoice.setPurpose(InvoicePurpose.BOOKING);
        invoice.setStatus(InvoiceStatus.PENDING);
        // Generate a unique invoice number
        invoice.setNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + request.bookingId());

        invoiceRepository.save(invoice);
        log.info("Invoice created for booking {}: {}", request.bookingId(), invoice.getNumber());
    }

    @Override
    public boolean hasUnpaidInvoices(Long userId) {
        return invoiceRepository.existsByUserIdAndStatus(userId, InvoiceStatus.PENDING);
    }
}
