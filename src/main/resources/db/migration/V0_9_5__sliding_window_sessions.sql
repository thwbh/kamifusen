-- Update session table to use sliding window approach instead of isActive flag
-- This enables stateless operation where sessions are considered active based on recent activity

-- Add updated_at column to track last activity time
ALTER TABLE session ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Drop isActive-based indexes and column since we use time-based approach
DROP INDEX IF EXISTS idx_session_is_active;
DROP INDEX IF EXISTS idx_session_active_start;

-- Remove columns that are not used in sliding window approach
ALTER TABLE session DROP COLUMN IF EXISTS is_active;
ALTER TABLE session DROP COLUMN IF EXISTS end_time;

-- Create new indexes for sliding window queries
CREATE INDEX idx_session_updated_at ON session(updated_at);
CREATE INDEX idx_session_visitor_updated ON session(visitor_id, updated_at);
