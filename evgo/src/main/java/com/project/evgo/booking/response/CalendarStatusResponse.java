package com.project.evgo.booking.response;

import com.project.evgo.sharedkernel.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarStatusResponse {
    private LocalDate date;
    private AvailabilityStatus status;
}
