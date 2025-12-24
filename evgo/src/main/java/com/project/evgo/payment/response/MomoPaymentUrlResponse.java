package com.project.evgo.payment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for MoMo payment URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentUrlResponse {

    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String orderId;
    private String requestId;
}
