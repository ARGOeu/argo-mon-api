-- ------------------------------------------------
-- Version: V1.17
-- Description: Migration that introduces the incident table
-- ------------------------------------------------

CREATE SEQUENCE incident_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE t_Incident (
    id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    service_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    incident_number VARCHAR(32) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_incident_number
        UNIQUE (incident_number),

    CONSTRAINT fk_incident_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES t_Tenant (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_incident_tenant_id
    ON t_Incident (tenant_id);

CREATE INDEX idx_incident_service_id
    ON t_Incident (service_id);

CREATE INDEX idx_incident_status
    ON t_Incident (status);

CREATE INDEX idx_incident_created_at
    ON t_Incident (created_at);