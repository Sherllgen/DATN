package com.project.evgo.payment.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBookingId(Long bookingId);

    List<Invoice> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, InvoiceStatus status);
}
