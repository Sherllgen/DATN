package com.project.evgo.sharedkernel.utils;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for geospatial operations with PostGIS.
 */
public class GeoUtils {

    /**
     * SRID 4326 = WGS84 coordinate system (standard for GPS coordinates)
     */
    private static final int SRID_WGS84 = 4326;

    /**
     * Geometry factory for creating PostGIS Point objects
     */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    /**
     * Create a PostGIS Point from latitude and longitude.
     * 
     * @param latitude  GPS latitude (-90 to 90)
     * @param longitude GPS longitude (-180 to 180)
     * @return PostGIS Point geometry
     * @throws AppException if coordinates are null or invalid
     */
    public static Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
        if (!isValidLatitude(latitude) || !isValidLongitude(longitude)) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
        // Note: PostGIS Point is (longitude, latitude) not (latitude, longitude)
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Validate latitude value.
     * 
     * @param latitude GPS latitude
     * @return true if latitude is between -90 and 90 (inclusive)
     */
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validate longitude value.
     * 
     * @param longitude GPS longitude
     * @return true if longitude is between -180 and 180 (inclusive)
     */
    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Convert meters to kilometers.
     * 
     * @param meters distance in meters
     * @return distance in kilometers, or null if input is null
     */
    public static Double metersToKm(Double meters) {
        return meters != null ? meters / 1000.0 : null;
    }

    /**
     * Convert kilometers to meters.
     * 
     * @param kilometers distance in kilometers
     * @return distance in meters, or null if input is null
     */
    public static Double kmToMeters(Double kilometers) {
        return kilometers != null ? kilometers * 1000.0 : null;
    }

    /**
     * Round distance to 2 decimal places.
     * 
     * @param distance distance value
     * @return rounded distance, or null if input is null
     */
    public static Double roundDistance(Double distance) {
        return distance != null ? Math.round(distance * 100.0) / 100.0 : null;
    }
}
