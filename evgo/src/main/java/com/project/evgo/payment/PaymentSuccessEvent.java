package com.project.evgo.payment;

/**
 * Domain event published when a ZaloPay or other payment method is successfully processed.
 */
public record PaymentSuccessEvent(Long invoiceId, String appTransId, String zpTransId, Long bookingId, Long chargingSessionId) {
}
