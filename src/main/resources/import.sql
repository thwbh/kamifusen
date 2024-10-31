insert into api_user (id, username, role) values (gen_random_uuid(), 'admin', 'app-admin');

insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '/test/path-1', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '/test/path-2', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '/test/path-3', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '/test/path-4', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '/test/path-5', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '/test/path-6', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '/test/path-7', NOW());
insert into page (id, path, page_added) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '/test/path-8', NOW());

insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a641', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a642', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a643', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a644', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a645', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a646', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a647', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a648', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a649', 'redacted');
insert into visitor (id, info) VALUES ('9f685bd0-90e6-479a-99b6-2fad28d2a640', 'redacted');

-- 7 visitors for the first page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a641');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a642');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a643');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a644');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a645');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a646');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a641', '9f685bd0-90e6-479a-99b6-2fad28d2a647');

-- 2 visitors for the second page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a648');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a642', '9f685bd0-90e6-479a-99b6-2fad28d2a649');

-- 1 visitor for the third page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a643', '9f685bd0-90e6-479a-99b6-2fad28d2a640');

-- 9 visitors for the fourth page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a641');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a642');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a643');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a645');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a646');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a647');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a648');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a649');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a644', '9f685bd0-90e6-479a-99b6-2fad28d2a640');

-- 4 visitors for the fifth page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a641');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a642');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a643');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a645', '9f685bd0-90e6-479a-99b6-2fad28d2a644');

-- 3 visitors for the sixth page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '9f685bd0-90e6-479a-99b6-2fad28d2a645');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '9f685bd0-90e6-479a-99b6-2fad28d2a646');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a646', '9f685bd0-90e6-479a-99b6-2fad28d2a647');

-- 3 visitors for the seventh page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a648');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a649');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a647', '9f685bd0-90e6-479a-99b6-2fad28d2a640');

-- 7 visitors for the eighth page
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a641');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a642');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a643');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a644');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a645');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a646');
insert into page_visit (page_id, visitor_id) values ('9f685bd0-90e6-479a-99b6-2fad28d2a648', '9f685bd0-90e6-479a-99b6-2fad28d2a647');