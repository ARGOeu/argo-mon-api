-- ------------------------------------------------
-- Version: V1.6
-- Description: Create t_Contact table for status pages
-- ------------------------------------------------

CREATE TABLE t_Contact (
    id VARCHAR(36) PRIMARY KEY,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_type SMALLINT NOT NULL,
    CONSTRAINT uq_contact_name_email_type UNIQUE (contact_name, contact_email, contact_type)

);

CREATE TABLE tenant_contact (
    tenant_id VARCHAR(36) NOT NULL,
    contact_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (tenant_id, contact_id),
    FOREIGN KEY (tenant_id) REFERENCES t_Tenant(id),
    FOREIGN KEY (contact_id) REFERENCES t_Contact(id)
);