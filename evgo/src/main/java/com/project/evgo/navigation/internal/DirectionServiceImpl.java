package com.project.evgo.navigation.internal;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.navigation.DirectionService;
import com.project.evgo.navigation.response.RouteResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectionServiceImpl implements DirectionService {

    private final WebClient.Builder webClientBuilder;
    private static final String OSRM_API_URL = "http://router.project-osrm.org/route/v1/driving/";

    @Override
    public RouteResponse getRoute(Double originLat, Double originLng, Double destLat, Double destLng) {
        // OSRM expects: {longitude},{latitude};{longitude},{latitude}
        String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                originLng, originLat, destLng, destLat);

        String url = OSRM_API_URL + coordinates + "?overview=full&geometries=polyline";

        try {
            String responseBody = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode response = mapper.readTree(responseBody);

            if (response == null || !response.has("routes") || response.get("routes").isEmpty()) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }

            JsonNode route = response.get("routes").get(0);
            String geometry = route.get("geometry").asText();
            double distance = route.get("distance").asDouble();
            double duration = route.get("duration").asDouble();

            return RouteResponse.builder()
                    .encodedPolyline(geometry)
                    .distance(distance)
                    .duration(duration)
                    .build();

        } catch (WebClientResponseException e) {
            log.error("OSRM API Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            // Handle HTTP errors from OSRM
            if (e.getStatusCode().is5xxServerError()) {
                throw new AppException(ErrorCode.NAVIGATION_SERVICE_UNAVAILABLE);
            }
            throw new AppException(ErrorCode.ROUTE_CALCULATION_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error fetching route: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ROUTE_CALCULATION_FAILED);
        }
    }
}
