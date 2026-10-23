-- ------------------------------------------------
-- Version: V1.22
-- Description: Add publicDowntime flag to t_Tenant
-- ------------------------------------------------

ALTER TABLE t_Tenant
    ADD COLUMN public_downtime BOOLEAN NOT NULL DEFAULT FALSE;
