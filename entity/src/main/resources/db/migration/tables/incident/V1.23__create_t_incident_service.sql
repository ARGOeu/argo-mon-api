-- ------------------------------------------------
-- Version: V1.23
-- Description: Support multiple affected services per incident
-- ------------------------------------------------

DROP INDEX IF EXISTS idx_incident_service_id;

ALTER TABLE t_Incident
    DROP COLUMN service_id,
    DROP COLUMN service_name;

CREATE TABLE t_Incident_Service (
    id VARCHAR(255) NOT NULL,
    incident_id VARCHAR(255) NOT NULL,
    service_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_incident_service_incident
        FOREIGN KEY (incident_id)
        REFERENCES t_Incident (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_incident_service
        UNIQUE (incident_id, service_id)
);

CREATE INDEX idx_incident_service_service_id
    ON t_Incident_Service (service_id);