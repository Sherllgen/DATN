package com.project.evgo.payment.internal;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
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
    @Transactional
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

    @Override
    public InvoiceResponse findById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        return invoiceDtoConverter.convert(invoice);
    }

    @Override
    public PageResponse<InvoiceResponse> getMyInvoices(Long userId, InvoiceStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invoice> invoices = invoiceRepository.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.of(invoices.map(invoiceDtoConverter::convert));
    }
}
