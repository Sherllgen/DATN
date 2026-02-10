package com.project.evgo.station.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Station entity.
 * Internal - not accessible by other modules.
 */
public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    boolean existsByNameAndOwnerIdAndDeletedAtIsNull(String name, Long ownerId);

    boolean existsByNameAndOwnerIdAndIdNotAndDeletedAtIsNull(String name, Long ownerId, Long id);

    Optional<Station> findByIdAndDeletedAtIsNull(Long id);

    List<Station> findAllByDeletedAtIsNull();

    /**
     * Find nearby stations within a radius using PostGIS geography.
     * OPTIMIZED: Uses KNN distance operator (<->) for index-friendly sorting.
     * Uses interface projection for clean result mapping.
     * 
     * @param latitude     GPS latitude
     * @param longitude    GPS longitude
     * @param radiusMeters radius in meters
     * @param maxResults   maximum number of results
     * @return List of StationProjection with distance in meters
     */
    @Query(value = """
            SELECT
                s.id AS id,
                s.owner_id AS ownerId,
                s.name AS name,
                s.description AS description,
                s.address AS address,
                s.latitude AS latitude,
                s.longitude AS longitude,
                s.rate AS rate,
                s.status AS status,
                s.is_flagged_low_quality AS isFlaggedLowQuality,
                s.created_at AS createdAt,
                s.updated_at AS updatedAt,
                s.location::geography <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS distance,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id) AS totalChargersCount,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id AND c.status = 'AVAILABLE') AS availableChargersCount
            FROM stations s
            WHERE s.deleted_at IS NULL
              AND s.status IN ('ACTIVE', 'INACTIVE')
              AND ST_DWithin(
                  s.location::geography,
                  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                  :radiusMeters
              )
            ORDER BY distance
            LIMIT :maxResults
            """, nativeQuery = true)
    List<StationProjection> findNearByStations(@Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Double radiusMeters,
            @Param("maxResults") Integer maxResults);

    /**
     * Search stations by text (name, address, description).
     * OPTIMIZED: Uses KNN operator when location is provided for efficient sorting.
     * Uses interface projection for clean result mapping.
     * 
     * @param query      search query
     * @param latitude   optional GPS latitude for distance sorting
     * @param longitude  optional GPS longitude for distance sorting
     * @param maxResults maximum number of results
     * @return List of StationProjection with optional distance in meters
     */
    @Query(value = """
            SELECT
                s.id AS id,
                s.owner_id AS ownerId,
                s.name AS name,
                s.description AS description,
                s.address AS address,
                s.latitude AS latitude,
                s.longitude AS longitude,
                s.rate AS rate,
                s.status AS status,
                s.is_flagged_low_quality AS isFlaggedLowQuality,
                s.created_at AS createdAt,
                s.updated_at AS updatedAt,
                CASE
                    WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL
                    THEN s.location::geography <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                    ELSE NULL
                END AS distance,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id) AS totalChargersCount,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id AND c.status = 'AVAILABLE') AS availableChargersCount
            FROM stations s
            WHERE s.deleted_at IS NULL
              AND s.status IN ('ACTIVE', 'INACTIVE')
              AND (
                  LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY
                CASE
                    WHEN :latitude IS NOT NULL AND :longitude IS NOT NULL
                    THEN s.location::geography <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                    ELSE NULL
                END NULLS LAST,
                s.created_at DESC
            LIMIT :maxResults
            """, nativeQuery = true)
    List<StationProjection> searchByText(@Param("query") String query,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("maxResults") Integer maxResults);

    /**
     * Find stations within a bounding box (map viewport).
     * OPTIMIZED: Uses PostGIS ST_MakeEnvelope and ST_Within for efficient spatial
     * queries.
     * Includes charger count subqueries to avoid N+1 problem.
     * Optionally calculates distance from a reference point (user location).
     * 
     * @param minLat     minimum latitude of bounding box
     * @param maxLat     maximum latitude of bounding box
     * @param minLng     minimum longitude of bounding box
     * @param maxLng     maximum longitude of bounding box
     * @param userLat    optional user latitude for distance calculation
     * @param userLng    optional user longitude for distance calculation
     * @param maxResults maximum number of results
     * @return List of StationProjection within the bounding box
     */
    @Query(value = """
            SELECT
                s.id AS id,
                s.owner_id AS ownerId,
                s.name AS name,
                s.description AS description,
                s.address AS address,
                s.latitude AS latitude,
                s.longitude AS longitude,
                s.rate AS rate,
                s.status AS status,
                s.is_flagged_low_quality AS isFlaggedLowQuality,
                s.created_at AS createdAt,
                s.updated_at AS updatedAt,
                CASE
                    WHEN :userLat IS NOT NULL AND :userLng IS NOT NULL
                    THEN s.location::geography <-> ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography
                    ELSE NULL
                END AS distance,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id) AS totalChargersCount,
                (SELECT COUNT(*) FROM chargers c WHERE c.station_id = s.id AND c.status = 'AVAILABLE') AS availableChargersCount
            FROM stations s
            WHERE s.deleted_at IS NULL
              AND s.status IN ('ACTIVE', 'INACTIVE')
              AND ST_Within(
                  s.location,
                  ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
              )
            ORDER BY
                CASE
                    WHEN :userLat IS NOT NULL AND :userLng IS NOT NULL
                    THEN s.location::geography <-> ST_SetSRID(ST_MakePoint(:userLng, :userLat), 4326)::geography
                    ELSE NULL
                END NULLS LAST,
                s.created_at DESC
            LIMIT :maxResults
            """, nativeQuery = true)
    List<StationProjection> findStationsInBound(@Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("maxResults") Integer maxResults);
}
