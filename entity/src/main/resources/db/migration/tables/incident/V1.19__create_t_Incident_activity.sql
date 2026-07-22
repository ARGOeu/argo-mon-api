-- ------------------------------------------------
-- Version: V1.18
-- Description: Migration that introduces incident activity history
-- ------------------------------------------------

CREATE TABLE t_Incident_Activity (
    id VARCHAR(255) NOT NULL,
    incident_id VARCHAR(255) NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_incident_activity_incident
        FOREIGN KEY (incident_id)
        REFERENCES t_Incident (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_incident_activity_incident_id
    ON t_Incident_Activity (incident_id);

CREATE INDEX idx_incident_activity_created_at
    ON t_Incident_Activity (created_at);