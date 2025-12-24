package com.project.evgo.station.internal;

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
 * PriceSetting entity - pricing configuration for a station.
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

    @Column(name = "charging_price_per_kwh", nullable = false, precision = 12, scale = 2)
    private BigDecimal chargingPricePerKwh;

    @Column(name = "booking_price", precision = 12, scale = 2)
    private BigDecimal bookingPrice;

    @Column(name = "penalty_price", precision = 12, scale = 2)
    private BigDecimal penaltyPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
