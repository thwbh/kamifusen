-- Populate development data with realistic timestamps
-- This migration adds test data with varied timestamps for development and testing

-- Insert admin user
INSERT INTO api_user (id, username, role, password)
VALUES (gen_random_uuid(), 'admin', 'app-admin', '$2a$10$1M/kyr.zOz6y9Owsp8qDUul1RmUfaI0zapjZED4wdwO1nLZ3Jz7OW');

-- Insert test pages with varied domains
INSERT INTO page (id, path, domain, page_added) VALUES
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '/', 'blog.tohuwabohu.io', NOW() - INTERVAL '30 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '/contact', 'poindl.info', NOW() - INTERVAL '25 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '/experience', 'poindl.info', NOW() - INTERVAL '20 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '/git', 'poindl.info', NOW() - INTERVAL '15 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '/git/config', 'poindl.info', NOW() - INTERVAL '10 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '/imprint', 'poindl.info', NOW() - INTERVAL '5 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '/imprint/', 'poindl.info', NOW() - INTERVAL '3 days'),
                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '/', 'poindl.info', NOW() - INTERVAL '1 day');

-- Insert test visitors with realistic data
INSERT INTO visitor (id, info, first_seen, user_agent, country) VALUES
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a641', 'redacted_hash_1', NOW() - INTERVAL '30 days', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'US'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a642', 'redacted_hash_2', NOW() - INTERVAL '29 days', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'DE'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a643', 'redacted_hash_3', NOW() - INTERVAL '28 days', 'Mozilla/5.0 (X11; Linux x86_64)', 'FR'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a644', 'redacted_hash_4', NOW() - INTERVAL '25 days', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0)', 'GB'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a645', 'redacted_hash_5', NOW() - INTERVAL '20 days', 'Mozilla/5.0 (Android 12; Mobile)', 'CA'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a646', 'redacted_hash_6', NOW() - INTERVAL '15 days', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'JP'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a647', 'redacted_hash_7', NOW() - INTERVAL '10 days', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'AU'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a648', 'redacted_hash_8', NOW() - INTERVAL '5 days', 'Mozilla/5.0 (X11; Linux x86_64)', 'NL'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a649', 'redacted_hash_9', NOW() - INTERVAL '3 days', 'Mozilla/5.0 (iPad; CPU OS 16_0)', 'SE'),
                                                                    ('9f685bd0-90e6-479a-99b6-2fad28d2a640', 'redacted_hash_10', NOW() - INTERVAL '1 day', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'NO');

-- Insert page visits with realistic timestamps (spread over the last 30 days)
-- Blog posts get the most traffic
INSERT INTO page_visit (page_id, visitor_id, visited_at) VALUES
-- Blog traffic (25 visits)
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '30 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '29 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '28 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a644', NOW() - INTERVAL '25 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a645', NOW() - INTERVAL '20 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a646', NOW() - INTERVAL '15 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a647', NOW() - INTERVAL '10 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a648', NOW() - INTERVAL '7 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a649', NOW() - INTERVAL '5 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a640', NOW() - INTERVAL '3 days'),

-- Main page traffic (82 visits) - most popular
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '30 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '29 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '28 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a644', NOW() - INTERVAL '27 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a645', NOW() - INTERVAL '26 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a646', NOW() - INTERVAL '25 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a647', NOW() - INTERVAL '24 days'),

-- Contact page (20 visits)
('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a648', NOW() - INTERVAL '25 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a649', NOW() - INTERVAL '24 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a640', NOW() - INTERVAL '23 days'),

-- Experience page (16 visits)
('9f685bd0-90e6-479a-99b6-2fad28d2a643', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '20 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a643', '9f685bd0-90e6-479a-99b6-2fad28d2a642', NOW() - INTERVAL '19 days'),

-- Git page (1 visit)
('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a643', NOW() - INTERVAL '15 days'),

-- Git config page (3 visits)
('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a644', NOW() - INTERVAL '10 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a645', NOW() - INTERVAL '9 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a646', NOW() - INTERVAL '8 days'),

-- Imprint page (1 visit)
('9f685bd0-90e6-479a-99b6-2fad28d2a646', '9f685bd0-90e6-479a-99b6-2fad28d2a647', NOW() - INTERVAL '5 days'),

-- Imprint with slash (4 visits)
('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a648', NOW() - INTERVAL '3 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a649', NOW() - INTERVAL '2 days'),
('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a640', NOW() - INTERVAL '1 day'),
('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a641', NOW() - INTERVAL '12 hours');