package com.project.evgo.navigation.response;

import lombok.Builder;

@Builder
public record RouteResponse(
        String encodedPolyline,
        Double distance,
        Double duration) {

    // Utility method to convert encoded polyline to PostGIS geography (commented as
    // requested)
    /*
     * public static String toPostGISString(String encodedPolyline) {
     * // Requires PostGIS extension installed in DB
     * // SQL: ST_LineFromEncodedPolyline(encodedPolyline)
     * return "ST_LineFromEncodedPolyline('" + encodedPolyline + "')";
     * }
     */
}
