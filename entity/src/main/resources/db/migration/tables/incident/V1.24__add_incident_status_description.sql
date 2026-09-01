-- ------------------------------------------------
-- Version: V1.24
-- Description: Add status description to incidents and incident activity
-- ------------------------------------------------

ALTER TABLE t_Incident
    ADD COLUMN status_description TEXT;

ALTER TABLE t_Incident_Activity
    ADD COLUMN status_description TEXT;

ALTER TABLE t_Incident
    ALTER COLUMN status SET DEFAULT 'NEW';