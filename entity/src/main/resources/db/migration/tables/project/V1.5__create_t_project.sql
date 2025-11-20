-- ------------------------------------------------
-- Version: V1.5
-- Description: Migration that introduces the project table
-- -------------------------------------------------

-- project table
CREATE TABLE t_Project (
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    start_date TIMESTAMP,
    end_date  TIMESTAMP,
    sustainability_end_date TIMESTAMP,
    data_retention_policy TEXT,
    created_at TIMESTAMP DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id)
);