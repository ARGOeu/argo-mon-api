-- ------------------------------------------------
-- Version: V1.21
-- Description: Add performanceEnabled flag to t_Tenant
-- ------------------------------------------------

ALTER TABLE t_Tenant
    ADD COLUMN performance BOOLEAN NOT NULL DEFAULT FALSE;
