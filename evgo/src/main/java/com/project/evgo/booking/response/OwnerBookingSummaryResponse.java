package com.project.evgo.booking.response;

import com.project.evgo.sharedkernel.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight response DTO tailored for the Station Owner Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBookingSummaryResponse {

    private Long id;
    private Long userId;
    private String customerName;
    private Long stationId;
    private String stationName;
    private LocalDateTime createdAt;
    private BookingStatus status;
    private BigDecimal totalPrice;
}
