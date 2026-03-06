package com.project.evgo.payment;

import com.project.evgo.payment.request.ZaloPayCallbackRequest;
import com.project.evgo.payment.request.ZaloPayOrderRequest;
import com.project.evgo.payment.response.ZaloPayOrderResponse;
import com.project.evgo.payment.response.ZaloPayStatusResponse;

/**
 * Public service interface for ZaloPay payment operations.
 * Accessible by other modules.
 */
public interface ZaloPayService {

    /**
     * Creates a ZaloPay order for the given invoice and returns the
     * orderUrl / zp_trans_token for the App-to-App payment flow.
     */
    ZaloPayOrderResponse createOrder(ZaloPayOrderRequest request);

    /**
     * Handles the ZaloPay IPN (Instant Payment Notification) webhook.
     * Verifies the MAC using KEY2 and updates Invoice + Transaction status.
     */
    void handleCallback(ZaloPayCallbackRequest callbackRequest);

    /**
     * Queries the current status of a ZaloPay order by appTransId.
     * Used by mobile after returning from ZaloPay to verify payment success.
     */
    ZaloPayStatusResponse queryOrderStatus(String appTransId);
}
