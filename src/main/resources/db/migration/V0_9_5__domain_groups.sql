-- Add domain group tables for aggregating page hits across related domains

CREATE TABLE DomainGroup (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    parentDomain VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(512),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE DomainGroupMember (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domainGroupId UUID NOT NULL REFERENCES DomainGroup(id) ON DELETE CASCADE,
    childDomain VARCHAR(255) NOT NULL,
    added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(childDomain) -- Each domain can only belong to one group
);

-- Index for fast lookups during hit processing
CREATE INDEX idx_domain_group_member_child ON DomainGroupMember(childDomain);
CREATE INDEX idx_domain_group_parent ON DomainGroup(parentDomain);