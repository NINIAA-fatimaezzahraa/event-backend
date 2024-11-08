-- event-db.event_locations definition
DROP TABLE IF EXISTS event_locations;

create table event_locations (
    event_location_id 	BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name 				VARCHAR(100) NOT NULL,
    address 			VARCHAR(255) NOT NULL,
    city 				VARCHAR(50) NOT NULL,
    postal_code 		VARCHAR(50) NOT NULL,
    country 			VARCHAR(50) NOT NULL,
    CONSTRAINT event_location_pkey PRIMARY KEY (event_location_id)
);

-- Add indexes for quicker searches on address-related fields
CREATE INDEX idx_event_location_city ON event_locations(city);
CREATE INDEX idx_event_location_postal_code ON event_locations(postal_code);
CREATE INDEX idx_event_location_country ON event_locations(country);

COMMIT;
