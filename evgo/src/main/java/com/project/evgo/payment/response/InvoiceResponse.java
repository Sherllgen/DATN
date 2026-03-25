package com.project.evgo.payment.response;

import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private Long bookingId;
    private Long chargingSessionId;
    private Long userId;
    private String number;
    private BigDecimal totalCost;
    private InvoicePurpose purpose;
    private InvoiceStatus status;
    private LocalDateTime createdAt;
}
