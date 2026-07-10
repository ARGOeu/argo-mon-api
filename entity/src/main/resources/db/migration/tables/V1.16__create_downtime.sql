-- ------------------------------------------------
-- Create downtime table
-- ------------------------------------------------

CREATE TABLE t_downtime (
                          id VARCHAR(36) PRIMARY KEY,
                          tenant_id VARCHAR(255) NOT NULL,

                          name VARCHAR(255) NOT NULL,

                          severity VARCHAR(50) NOT NULL
                              CHECK (severity IN ('Outage', 'Warning')),

                          message TEXT,

                          scheduled_at TIMESTAMP NOT NULL,

                          completed_at TIMESTAMP,

                          classification VARCHAR(50) NOT NULL
                              CHECK (classification IN ('Scheduled', 'Unscheduled')),


                          created_at      TIMESTAMP DEFAULT NULL,
                          updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(255) NOT NULL,
                          updated_by VARCHAR(255)

);


-- ------------------------------------------------
-- Create downtime_service table
-- ------------------------------------------------

CREATE TABLE t_downtime_service_endpoint (
                                  id VARCHAR(36) PRIMARY KEY,

                                  downtime_id VARCHAR(36) NOT NULL,

                                  hostname VARCHAR(255) NOT NULL,

                                  service VARCHAR(255) NOT NULL,

                                  CONSTRAINT fk_downtime_service_endpoint
                                      FOREIGN KEY (downtime_id)
                                          REFERENCES t_downtime(id)
                                          ON DELETE CASCADE
);


-- ------------------------------------------------
-- Indexes
-- ------------------------------------------------

CREATE INDEX idx_downtime_scheduled_at
    ON t_downtime(scheduled_at);

CREATE INDEX idx_downtime_service_downtime_id
    ON t_downtime_service_endpoint(downtime_id);

CREATE INDEX idx_downtime_service_hostname
    ON t_downtime_service_endpoint(hostname);