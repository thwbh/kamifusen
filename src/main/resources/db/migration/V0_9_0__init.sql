-- Initial baseline schema for kamifusen
-- Creates the basic tables: api_user, page, visitor, and page_visit

-- API User table for authentication
CREATE TABLE api_user (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP
);

-- Page table for tracking website pages
CREATE TABLE page (
    id UUID PRIMARY KEY,
    path VARCHAR(2048) NOT NULL,
    domain VARCHAR(255),
    page_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_hit TIMESTAMP
);

-- Visitor table for unique visitor tracking
CREATE TABLE visitor (
    id UUID PRIMARY KEY,
    info VARCHAR(512) NOT NULL
);

-- Page visit junction table
CREATE TABLE page_visit (
    page_id UUID NOT NULL REFERENCES page(id) ON DELETE CASCADE,
    visitor_id UUID NOT NULL REFERENCES visitor(id) ON DELETE CASCADE,
    PRIMARY KEY (page_id, visitor_id)
);

-- Create indexes for performance
CREATE INDEX idx_page_path_domain ON page(path, domain);
CREATE INDEX idx_page_visit_page_id ON page_visit(page_id);
CREATE INDEX idx_page_visit_visitor_id ON page_visit(visitor_id);
CREATE INDEX idx_visitor_info ON visitor(info);