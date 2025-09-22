-- Add timestamp field to page_visit table for time-based analytics
-- This enables visit trends, time-range filtering, and temporal analysis

ALTER TABLE page_visit ADD COLUMN visited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Create index for time-based queries
CREATE INDEX idx_page_visit_visited_at ON page_visit(visited_at);
CREATE INDEX idx_page_visit_page_visited ON page_visit(page_id, visited_at);

-- Enhance visitor table with additional tracking fields
-- Enables visitor journey tracking, device analytics, and traffic source analysis

ALTER TABLE visitor ADD COLUMN first_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE visitor ADD COLUMN last_seen TIMESTAMP;
ALTER TABLE visitor ADD COLUMN user_agent VARCHAR(512);
ALTER TABLE visitor ADD COLUMN country VARCHAR(2);
ALTER TABLE visitor ADD COLUMN referrer VARCHAR(1024);

-- Create indexes for enhanced visitor analytics
CREATE INDEX idx_visitor_first_seen ON visitor(first_seen);
CREATE INDEX idx_visitor_last_seen ON visitor(last_seen);
CREATE INDEX idx_visitor_country ON visitor(country);

-- Update existing visitors with current timestamp for first_seen
UPDATE visitor SET first_seen = CURRENT_TIMESTAMP WHERE first_seen IS NULL;

-- Create session tracking table for real-time analytics
-- Enables active users tracking, average session time, and engagement metrics

CREATE TABLE session (
     id UUID PRIMARY KEY,
     visitor_id UUID NOT NULL REFERENCES visitor(id) ON DELETE CASCADE,
     start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     end_time TIMESTAMP,
     page_views INTEGER DEFAULT 0,
     is_active BOOLEAN DEFAULT TRUE
);

-- Create indexes for session analytics
CREATE INDEX idx_session_visitor_id ON session(visitor_id);
CREATE INDEX idx_session_start_time ON session(start_time);
CREATE INDEX idx_session_is_active ON session(is_active);
CREATE INDEX idx_session_active_start ON session(is_active, start_time) WHERE is_active = TRUE;

-- Create Blacklist table for hiding pages without actual deletion
CREATE TABLE Blacklist (
       id UUID PRIMARY KEY,
       pageId UUID NOT NULL,
       blacklistedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_blacklist_page FOREIGN KEY (pageId) REFERENCES Page(id)
);

-- Index for fast lookups by pageId
CREATE INDEX idx_blacklist_page_id ON Blacklist(pageId);

-- Ensure only one blacklist entry per page
CREATE UNIQUE INDEX idx_blacklist_unique_page ON Blacklist(pageId);