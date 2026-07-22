-- ------------------------------------------------
-- Version: V1.20
-- Description: Migration that makes completed_at mandatory
-- ------------------------------------------------

ALTER TABLE t_downtime
    ALTER COLUMN completed_at SET NOT NULL;