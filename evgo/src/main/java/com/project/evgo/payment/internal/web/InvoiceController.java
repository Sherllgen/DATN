package com.project.evgo.payment.internal.web;

import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
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

    private final InvoiceService invoiceService;

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get invoice by booking ID", description = "Fetches the invoice associated with a specific booking")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByBookingId(@PathVariable Long bookingId) {
        InvoiceResponse response = invoiceService.findByBookingId(bookingId);

        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get invoice by charging session ID", description = "Fetches the invoice associated with a specific charging session")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByChargingSessionId(@PathVariable Long sessionId) {
        InvoiceResponse response = invoiceService.findByChargingSessionId(sessionId);

        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(response)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create invoice", description = "Creates a new invoice")
    public ResponseEntity<ApiResponse<Void>> createInvoice(@RequestBody InvoiceCreatedRequest request) {
        invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .build());
    }
}
