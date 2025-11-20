-- ------------------------------------------------
-- Version: V1.4
-- Description: Drop t_User table and related indexes
-- ------------------------------------------------

-- Drop indexes first (if they exist)
DROP INDEX IF EXISTS idx_unique_user_email;
DROP INDEX IF EXISTS idx_user_name;
DROP INDEX IF EXISTS idx_user_surname;

-- Drop table
DROP TABLE IF EXISTS t_User CASCADE;
