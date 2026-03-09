package com.project.evgo.payment.response;

/**
 * Response DTO returned to mobile after a ZaloPay order is successfully
 * created.
 *
 * @param orderUrl     Deep-link / URL that opens ZaloPay Sandbox app
 *                     (App-to-App flow).
 * @param zpTransToken ZaloPay App-to-App token; alternative to orderUrl on
 *                     Android.
 * @param appTransId   Our unique transaction ID (format: yyMMdd_UUID) used for
 *                     status queries.
 */
public record ZaloPayOrderResponse(
        String orderUrl,
        String zpTransToken,
        String appTransId) {
}
