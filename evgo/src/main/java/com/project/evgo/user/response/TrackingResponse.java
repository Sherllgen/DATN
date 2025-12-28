package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import lombok.Builder;

@Builder
public record TrackingResponse(
        StationOwnerStatus status,
        String message
) {
}
