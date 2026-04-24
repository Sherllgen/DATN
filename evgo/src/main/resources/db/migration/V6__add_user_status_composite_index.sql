-- ============================================================================
-- V6: Add composite index on charging_sessions(user_id, status)
-- ============================================================================
-- Purpose: Optimize the paginated queries used by:
--   - GET /api/v1/charging/me/active   (findFirstByUserIdAndStatusIn)
--   - GET /api/v1/charging/me/history  (findByUserIdAndStatus + Pageable)
--
-- Without this index, PostgreSQL uses the single-column idx_session_user_id
-- to find all sessions for a user, then filters by status in memory.
-- The composite index allows direct index-only lookups on (user_id, status).

-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_session_user_status
    ON public.charging_sessions (user_id, status);
