-- ------------------------------------------------
-- Version: V1.26
-- Description: Add display_supergroup_results flag to t_Tenant
-- ------------------------------------------------
ALTER TABLE t_Tenant
ADD COLUMN display_supergroup_results BOOLEAN NOT NULL DEFAULT FALSE;