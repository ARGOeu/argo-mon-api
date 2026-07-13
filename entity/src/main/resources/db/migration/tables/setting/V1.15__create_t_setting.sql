-- ------------------------------------------------
-- Version: V1.15
-- Description: Create t_Setting table with final schema
-- ------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE t_Setting (
                           id VARCHAR(36) PRIMARY KEY,
                           setting_enable BOOLEAN DEFAULT FALSE,
                           setting_data JSONB NOT NULL,
                           updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_by VARCHAR(255)
);

-- Performance Data settings
INSERT INTO t_Setting (
    id,
    setting_enable,
    setting_data,
    updated_by
)
VALUES (
           gen_random_uuid()::text,
           FALSE,
           jsonb_build_object(
                   'label', 'Performance Monitoring',
                   'description', 'Configuration for performance monitoring',
                   'config', jsonb_build_object(
                           'base.url', NULL
                             )
           ),
           NULL
       );