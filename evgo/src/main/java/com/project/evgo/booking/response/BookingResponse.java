package com.project.evgo.booking.response;

import com.project.evgo.sharedkernel.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.project.evgo.sharedkernel.enums.ConnectorType;

/**
 * Response DTO for booking information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long userId;
    private Long stationId;
    private Long chargerId;
    private Integer portNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    
    // Flattened fields for UI
    private String stationName;
    private String stationAddress;
    private String chargerName;
    private ConnectorType connectorType;
    private Double maxPower;
}
