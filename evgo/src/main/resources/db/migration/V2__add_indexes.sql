-- Migration: Optimized PostGIS support with performance improvements
-- Description: Enables PostGIS extension, adds geometry column to stations table,
--              creates FUNCTIONAL INDEX on geography cast, optimized trigger with
--              conditional updates and NULL handling, and uses KNN distance operators

-- ====================
-- 1. ENABLE POSTGIS EXTENSION
-- ====================
CREATE EXTENSION IF NOT EXISTS postgis;

-- ====================
-- 2. ADD GEOMETRY COLUMN
-- ====================
-- Add location column with Point geometry type, SRID 4326 (WGS84 coordinate system)
ALTER TABLE stations 
ADD COLUMN IF NOT EXISTS location geometry(Point, 4326);

-- ====================
-- 3. MIGRATE EXISTING DATA
-- ====================
-- Populate location column from existing latitude/longitude values
-- Note: PostGIS Point is (longitude, latitude) not (latitude, longitude)
UPDATE stations 
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE latitude IS NOT NULL 
  AND longitude IS NOT NULL
  AND location IS NULL;

-- ====================
-- 4. CREATE SPATIAL INDEXES
-- ====================

-- 4.1: Standard GIST index on geometry (for basic spatial operations)
CREATE INDEX IF NOT EXISTS idx_stations_location_geom 
ON stations USING GIST(location);

-- 4.2: FUNCTIONAL INDEX on geography cast (CRITICAL FOR PERFORMANCE!)
-- This index matches the `location::geography` cast used in WHERE and ORDER BY clauses
-- Without this, queries with ::geography casting will perform Sequential Scans
CREATE INDEX IF NOT EXISTS idx_stations_location_geog 
ON stations USING GIST((location::geography));

-- 4.3: Additional index on status for filtered queries
CREATE INDEX IF NOT EXISTS idx_stations_status 
ON stations(status) 
WHERE deleted_at IS NULL;

-- 4.4: Composite index for text search (for LIKE queries)
-- Using pg_trgm extension for fuzzy text matching (optional but recommended)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX IF NOT EXISTS idx_stations_text_search 
-- ON stations USING GIN (name gin_trgm_ops, address gin_trgm_ops, description gin_trgm_ops);

-- ====================
-- 5. CREATE OPTIMIZED SYNC TRIGGER
-- ====================
-- Function to automatically sync lat/lon changes to geometry column
-- OPTIMIZATIONS:
-- 1. Uses IS DISTINCT FROM to avoid unnecessary updates
-- 2. Handles NULL values gracefully
-- 3. Only computes new geometry when coordinates actually change
CREATE OR REPLACE FUNCTION sync_station_location()
RETURNS TRIGGER AS $$
BEGIN
  -- Check if latitude or longitude have actually changed
  -- IS DISTINCT FROM handles NULL comparisons correctly
  IF (NEW.latitude IS DISTINCT FROM OLD.latitude) 
     OR (NEW.longitude IS DISTINCT FROM OLD.longitude) THEN
    
    -- If both coordinates are present, create the Point geometry
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
      NEW.location = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    ELSE
      -- If either coordinate is NULL, set location to NULL (graceful degradation)
      NEW.location = NULL;
    END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if it exists (for migration reruns)
DROP TRIGGER IF EXISTS trg_sync_station_location ON stations;

-- Create trigger to run before INSERT or UPDATE
-- Note: For INSERT, OLD is NULL, so IS DISTINCT FROM will always be true
CREATE TRIGGER trg_sync_station_location
BEFORE INSERT OR UPDATE ON stations
FOR EACH ROW
EXECUTE FUNCTION sync_station_location();

-- ====================
-- 6. VERIFICATION QUERIES
-- ====================

-- 6.1: Verify PostGIS version
-- SELECT postgis_version();

-- 6.2: Verify indexes are created
-- SELECT 
--     indexname, 
--     indexdef 
-- FROM pg_indexes 
-- WHERE tablename = 'stations' 
--   AND indexname LIKE 'idx_stations_location%';

-- 6.3: Test if FUNCTIONAL INDEX is used (should show "Index Scan using idx_stations_location_geog")
-- EXPLAIN ANALYZE
-- SELECT id, name
-- FROM stations
-- WHERE ST_DWithin(
--   location::geography,
--   ST_SetSRID(ST_MakePoint(106.660172, 10.762622), 4326)::geography,
--   5000
-- );

-- 6.4: Test KNN distance operator (should show "Index Scan using idx_stations_location_geog")
-- EXPLAIN ANALYZE  
-- SELECT id, name,
--        location::geography <-> ST_SetSRID(ST_MakePoint(106.660172, 10.762622), 4326)::geography as distance_m
-- FROM stations
-- WHERE location IS NOT NULL
-- ORDER BY location::geography <-> ST_SetSRID(ST_MakePoint(106.660172, 10.762622), 4326)::geography
-- LIMIT 10;

-- 6.5: Verify trigger optimization (update without coordinate change - should NOT update location)
-- UPDATE stations SET description = 'Updated description' WHERE id = 1;
-- -- Check if location was updated (should show no change)
-- SELECT id, name, latitude, longitude, ST_AsText(location), updated_at FROM stations WHERE id = 1;

-- 6.6: Test NULL handling (set coordinates to NULL)
-- UPDATE stations SET latitude = NULL WHERE id = 1;
-- -- Verify location is also NULL
-- SELECT id, name, latitude, longitude, location FROM stations WHERE id = 1;

-- ====================
-- 7. REVIEWS CONSTRAINTS
-- ====================

-- 7.1: Unique constraint for one user - one review per station
-- This prevents a user from posting multiple reviews for the same station.
-- If they post again, the service layer will handle the update (Upsert logic).
ALTER TABLE reviews 
ADD CONSTRAINT uk_user_station_review UNIQUE (user_id, station_id);
