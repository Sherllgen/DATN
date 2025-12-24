package com.project.evgo.payment.internal;

import com.project.evgo.payment.response.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Payment entity to DTO.
 */
@Component
public class PaymentDtoConverter {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        return payments.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<PaymentResponse> toResponse(Optional<Payment> payment) {
        return payment.map(this::toResponse);
    }
}
