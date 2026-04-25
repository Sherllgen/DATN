package com.project.evgo.charging.internal;

import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ChargingSession entity.
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "charging_sessions", indexes = {
        @Index(name = "idx_session_port_status", columnList = "port_id, status"),
        @Index(name = "idx_session_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_session_user_id", columnList = "user_id"),
        @Index(name = "idx_session_user_status", columnList = "user_id, status")
})
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "port_id", nullable = false)
    private Long portId;

    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "meter_start")
    private Integer meterStart;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_kwh", precision = 10, scale = 4)
    private BigDecimal totalKwh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargingSessionStatus status = ChargingSessionStatus.PREPARING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
