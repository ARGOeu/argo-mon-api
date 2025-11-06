-- ------------------------------------------------
-- Version: V1.2
-- Description: Migration that introduces the user table
-- -------------------------------------------------

-- user table
CREATE TABLE t_User (
   id varchar(255) NOT NULL,
   username varchar(255) DEFAULT NULL,
   name varchar(255) DEFAULT NULL,
   surname varchar(255) DEFAULT NULL,
   email varchar(100) DEFAULT NULL,
   updated_at timestamp DEFAULT NULL,
   registered_at timestamp DEFAULT NULL,
   PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_unique_user_email ON t_User (email);
CREATE INDEX idx_user_name ON t_User (name);
CREATE INDEX idx_user_surname ON t_User (surname);
