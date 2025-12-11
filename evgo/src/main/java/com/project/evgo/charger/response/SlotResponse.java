package com.project.evgo.charger.response;

import com.project.evgo.sharedkernel.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for slot information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private Long id;
    private Integer slotNumber;
    private SlotStatus status;
    private Long chargerId;
    private LocalDateTime createdAt;
}
