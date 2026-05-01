package com.project.evgo.payment.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.project.evgo.sharedkernel.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBookingId(Long bookingId);

    Optional<Invoice> findByChargingSessionId(Long chargingSessionId);

    List<Invoice> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, InvoiceStatus status);

    Page<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(i.totalCost) FROM Invoice i WHERE i.bookingId IN :bookingIds AND i.status = :status")
    BigDecimal sumAmountByBookingIdInAndStatus(
            @Param("bookingIds") List<Long> bookingIds,
            @Param("status") InvoiceStatus status);
}
