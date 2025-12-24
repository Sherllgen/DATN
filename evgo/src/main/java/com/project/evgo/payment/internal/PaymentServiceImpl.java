package com.project.evgo.payment.internal;

import com.project.evgo.payment.PaymentService;
import com.project.evgo.payment.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of PaymentService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDtoConverter converter;

    @Override
    public Optional<PaymentResponse> findById(Long id) {
        return converter.toResponse(paymentRepository.findById(id));
    }

    @Override
    public Optional<PaymentResponse> findByBookingId(Long bookingId) {
        return converter.toResponse(paymentRepository.findByBookingId(bookingId));
    }

    @Override
    public List<PaymentResponse> findByUserId(Long userId) {
        return converter.toResponseList(paymentRepository.findByUserId(userId));
    }
}
