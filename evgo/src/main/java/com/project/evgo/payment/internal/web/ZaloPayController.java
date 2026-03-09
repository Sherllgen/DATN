package com.project.evgo.payment.internal.web;

import com.project.evgo.payment.ZaloPayService;
import com.project.evgo.payment.request.ZaloPayCallbackRequest;
import com.project.evgo.payment.request.ZaloPayOrderRequest;
import com.project.evgo.payment.response.ZaloPayOrderResponse;
import com.project.evgo.payment.response.ZaloPayStatusResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for ZaloPay payment operations.
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>POST /api/v1/zalopay/orders – Create ZaloPay order (requires auth)</li>
 * <li>POST /api/v1/zalopay/callback – ZaloPay IPN webhook (public,
 * MAC-verified)</li>
 * <li>GET /api/v1/zalopay/orders/{id}/status – Query order status (requires
 * auth)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/zalopay")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ZaloPay", description = "ZaloPay App-to-App payment APIs")
public class ZaloPayController {

    private final ZaloPayService zaloPayService;

    @PostMapping("/orders")
    @Operation(summary = "Create ZaloPay order", description = "Creates a ZaloPay Sandbox order and returns the App-to-App orderUrl and token.")
    public ResponseEntity<ApiResponse<ZaloPayOrderResponse>> createOrder(
            @Valid @RequestBody ZaloPayOrderRequest request) {

        ZaloPayOrderResponse result = zaloPayService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ZaloPayOrderResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("ZaloPay order created")
                        .data(result)
                        .build());
    }

    /**
     * ZaloPay IPN callback — must remain PUBLIC (no JWT).
     * Security is provided by MAC signature verification inside the service.
     *
     * <p>
     * ZaloPay docs: callback endpoint must return JSON with {@code return_code} and
     * {@code return_message} to acknowledge receipt.
     */
    @PostMapping("/callback")
    @Operation(summary = "ZaloPay IPN webhook", description = "Receives ZaloPay payment result callback. Must be publicly accessible (MAC-verified internally).")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestBody ZaloPayCallbackRequest callbackRequest) {

        try {
            zaloPayService.handleCallback(callbackRequest);
            return ResponseEntity.ok(Map.of(
                    "return_code", 1,
                    "return_message", "success"));
        } catch (Exception e) {
            log.error("ZaloPay callback processing error", e);
            // Return 200 with error code so ZaloPay retries gracefully
            return ResponseEntity.ok(Map.of(
                    "return_code", 0,
                    "return_message", e.getMessage()));
        }
    }

    @GetMapping("/orders/{appTransId}/status")
    @Operation(summary = "Query ZaloPay order status", description = "Returns the current payment status for the given appTransId. Call after returning from ZaloPay app.")
    public ResponseEntity<ApiResponse<ZaloPayStatusResponse>> queryOrderStatus(
            @PathVariable String appTransId) {

        ZaloPayStatusResponse result = zaloPayService.queryOrderStatus(appTransId);
        return ResponseEntity.ok(ApiResponse.<ZaloPayStatusResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
