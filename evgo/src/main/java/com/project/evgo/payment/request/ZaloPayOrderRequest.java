package com.project.evgo.payment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO to create a ZaloPay order (App-to-App flow).
 *
 * @param invoiceId   ID of the invoice to pay.
 * @param userId      ID of the authenticated user.
 * @param amount      Amount in VND (must be positive).
 * @param description Human-readable order description shown in ZaloPay.
 */
public record ZaloPayOrderRequest(

        @NotNull(message = "Invoice ID is required") 
        Long invoiceId,

        @NotNull(message = "User ID is required") 
        Long userId,

        @NotNull(message = "Amount is required") 
        @Positive(message = "Amount must be positive") 
        BigDecimal amount,

        String description) {
}
