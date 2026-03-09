package com.project.evgo.payment.request;

/**
 * Webhook payload sent by ZaloPay to our callback endpoint.
 *
 * <p>
 * ZaloPay signs the {@code data} JSON string with KEY2 (HMAC-SHA256) and puts
 * the result in {@code mac}. We must verify this before trusting the event.
 *
 * @param data JSON string containing the transaction result (serialized by
 *             ZaloPay).
 * @param mac  HMAC-SHA256 of {@code data} using KEY2.
 * @param type Callback type (1 = payment result).
 */
public record ZaloPayCallbackRequest(
        String data,
        String mac,
        Integer type) {
}
