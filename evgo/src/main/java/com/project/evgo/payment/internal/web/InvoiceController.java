package com.project.evgo.payment.internal.web;

import com.project.evgo.payment.internal.Invoice;
import com.project.evgo.payment.internal.InvoiceRepository;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice lookup endpoints")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get invoice by booking ID", description = "Fetches the invoice associated with a specific booking")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByBookingId(@PathVariable Long bookingId) {
        Invoice invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        InvoiceResponse response = InvoiceResponse.builder()
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

        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(response)
                .build());
    }
}
