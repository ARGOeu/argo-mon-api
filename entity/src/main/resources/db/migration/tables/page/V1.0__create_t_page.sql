-- ------------------------------------------------
-- Version: V1.0
-- Description: Create t_Status_Page table for status pages
-- ------------------------------------------------

CREATE TABLE t_Status_Page (
    id              VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    user_id         VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    api             VARCHAR(1024) NOT NULL,
    secret          TEXT NOT NULL,
    report          VARCHAR(255) NOT NULL,
    config          JSONB NOT NULL,
    created_at      TIMESTAMP DEFAULT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_t_page_slug ON t_Status_Page(slug);
