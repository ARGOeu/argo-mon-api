-- ------------------------------------------------
-- Version: V1.3
-- Description: Create t_Tenant table for status pages
-- ------------------------------------------------
CREATE TABLE t_Tenant (
    id              VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    email           VARCHAR(255) ,
    description     VARCHAR(1024) ,
    website         VARCHAR(255) ,
    image           VARCHAR(255) ,
    updated_by      VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
