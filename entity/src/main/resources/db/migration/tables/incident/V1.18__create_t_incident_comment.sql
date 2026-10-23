-- ------------------------------------------------
-- Version: V1.18
-- Description: Migration that introduces incident comments
-- ------------------------------------------------

CREATE TABLE t_Incident_Comment (
    id VARCHAR(255) NOT NULL,
    incident_id VARCHAR(255) NOT NULL,
    comment TEXT NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_incident_comment_incident
        FOREIGN KEY (incident_id)
        REFERENCES t_Incident (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_incident_comment_incident_id
    ON t_Incident_Comment (incident_id);

CREATE INDEX idx_incident_comment_created_at
    ON t_Incident_Comment (created_at);