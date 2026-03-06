package com.project.evgo.payment.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByAppTransId(String appTransId);

    List<Transaction> findByInvoiceId(Long invoiceId);
}
