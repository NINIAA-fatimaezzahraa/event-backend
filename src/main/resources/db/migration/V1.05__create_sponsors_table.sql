-- event-db.sponsors definition
DROP TABLE IF EXISTS sponsors;

CREATE TABLE sponsors (
    sponsor_id 			BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    name 				VARCHAR(100) NOT NULL UNIQUE,
    description 		VARCHAR(255) NOT null,
    logo 			VARCHAR(255) NOT null,
    CONSTRAINT sponsor_pkey PRIMARY KEY (sponsor_id)
);

-- Add an index for quicker searches on sponsor name
CREATE INDEX idx_sponsor_name ON sponsors(name);

COMMIT;
