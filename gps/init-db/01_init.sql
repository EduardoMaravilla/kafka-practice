CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Aprovecha para crear la tabla y la hypertable de una vez
CREATE TABLE IF NOT EXISTS gps_events
(
    id              SERIAL,
    device_id       VARCHAR(50)      NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    event_timestamp TIMESTAMPTZ      NOT NULL,
    PRIMARY KEY (id, event_timestamp)
);

SELECT create_hypertable('gps_events', 'event_timestamp', if_not_exists => TRUE);