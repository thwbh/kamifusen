-- Populate development data with realistic timestamps
-- This migration adds test data with varied timestamps for development and testing

-- Insert admin user
INSERT INTO api_user (id, username, role, password)
VALUES (gen_random_uuid(), 'admin', 'app-admin', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW');

-- Insert API keys for testing
INSERT INTO api_user (id, username, role, password, expires_at) VALUES
    ('9f685bd0-90e6-479a-99b6-3fad28d2a001', 'api-user-1', 'api-user', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW', NOW() + INTERVAL '1 year'),
    ('9f685bd0-90e6-479a-99b6-3fad28d2a003', 'api-user-2', 'api-user', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW', NOW() + INTERVAL '6 months'),
    ('9f685bd0-90e6-479a-99b6-3fad28d2a004', 'api-user-3', 'api-user', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW', NOW() - INTERVAL '3 months'),
    ('9f685bd0-90e6-479a-99b6-3fad28d2a005', 'api-user-5', 'api-user', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW', NOW() + INTERVAL '3 months')
;

-- Insert test pages with varied domains
INSERT INTO page (id, path, domain, page_added) VALUES
    ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '/', 'domain-1.test', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '/', 'domain-2.test', NOW() - INTERVAL '25 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '/', 'domain-3.test', NOW() - INTERVAL '20 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '/path-1', 'domain-1.test', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '/path-2', 'domain-1.test', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '/path-1', 'domain-2.test', NOW() - INTERVAL '25 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '/path-2', 'domain-2.test', NOW() - INTERVAL '25 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '/path-1', 'domain-3.test', NOW() - INTERVAL '20 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a649', '/path-2', 'domain-3.test', NOW() - INTERVAL '20 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a650', '/deletion', 'delete.test', NOW() - INTERVAL '20 days')
;

-- Insert test visitors
INSERT INTO visitor (id, info, first_seen, user_agent, country) VALUES
    ('9f685bd0-90e6-479a-99b6-2fad28d2a641', 'redacted_hash_1', NOW() - INTERVAL '30 days', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'US'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', 'redacted_hash_2', NOW() - INTERVAL '29 days', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'DE'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', 'redacted_hash_3', NOW() - INTERVAL '28 days', 'Mozilla/5.0 (X11; Linux x86_64)', 'FR');;

-- Insert page visits with realistic timestamps (spread over the last 30 days)
-- Blog posts get the most traffic
INSERT INTO page_visit (page_id, visitor_id, visited_at) VALUES
-- visited by redacted_hash_1
    ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '30 days'),
-- visited by redacted_hash_2
    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '30 days'),
-- visited by redacted_hash_3
    ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '30 days'),
    ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '30 days')
;