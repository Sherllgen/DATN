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
                s.location::geography <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS distance
            FROM stations s
            WHERE s.deleted_at IS NULL
              AND s.status = 'ACTIVE'
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
                END AS distance
            FROM stations s
            WHERE s.deleted_at IS NULL
              AND s.status = 'ACTIVE'
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
}
