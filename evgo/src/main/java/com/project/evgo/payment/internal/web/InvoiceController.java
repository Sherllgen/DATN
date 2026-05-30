package com.project.evgo.payment.internal.web;

import com.project.evgo.payment.InvoiceService;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;

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

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Fetches a specific invoice by its ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.findById(id);

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

    @GetMapping("/unpaid/check")
    @Operation(summary = "Check unpaid invoices", description = "Checks if the current user has any unpaid invoices")
    public ResponseEntity<ApiResponse<Boolean>> checkUnpaidInvoices() {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean hasUnpaid = invoiceService.hasUnpaidInvoices(userId);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(hasUnpaid)
                .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get my invoices", description = "Fetches a paginated list of invoices for the current user, filtered by status and optional purpose")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getMyInvoices(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(required = false) String purpose,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();

        InvoiceStatus invoiceStatus;
        try {
            invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            invoiceStatus = InvoiceStatus.PENDING;
        }

        InvoicePurpose invoicePurpose = null;
        if (purpose != null && !purpose.isBlank()) {
            try {
                invoicePurpose = InvoicePurpose.valueOf(purpose.toUpperCase());
            } catch (IllegalArgumentException e) {
                invoicePurpose = null; // Invalid purpose string → no filter
            }
        }

        PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(userId, invoiceStatus, invoicePurpose, page, size);
        return ResponseEntity.ok(ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
