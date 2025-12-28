package com.project.evgo.user;

import com.project.evgo.user.request.TrackingRequest;
import com.project.evgo.user.response.TrackingResponse;

public interface StationOwnerService {
    TrackingResponse getStatus(TrackingRequest request);
}
