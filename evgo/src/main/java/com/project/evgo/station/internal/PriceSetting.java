package com.project.evgo.station.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PriceSetting entity - versioned pricing configuration for a station.
 * Each update creates a new version (immutable records for audit trail).
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "price_settings")
public class PriceSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "charging_rate_per_kwh", nullable = false, precision = 12, scale = 2)
    private BigDecimal chargingRatePerKwh;

    @Column(name = "booking_fee", precision = 12, scale = 2)
    private BigDecimal bookingFee;

    @Column(name = "idle_penalty_per_minute", precision = 12, scale = 2)
    private BigDecimal idlePenaltyPerMinute;

    @Column(name = "grace_period_minutes", nullable = false)
    private Integer gracePeriodMinutes = 30;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String notes;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
