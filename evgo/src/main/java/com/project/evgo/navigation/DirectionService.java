package com.project.evgo.navigation;

import com.project.evgo.navigation.response.RouteResponse;

public interface DirectionService {
    RouteResponse getRoute(Double originLat, Double originLng, Double destLat, Double destLng);
}
