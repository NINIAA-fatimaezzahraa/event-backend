-- event-db.event_sponsors definition
DROP TABLE IF EXISTS event_sponsors;

CREATE TABLE event_sponsors (
    event_id        BIGINT NOT NULL,
    sponsor_id      BIGINT NOT NULL,
    CONSTRAINT event_sponsor_pkey PRIMARY KEY (event_id, sponsor_id),
    CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_sponsor_id FOREIGN KEY (sponsor_id) REFERENCES sponsors(sponsor_id) ON DELETE CASCADE
);

-- Add indexes to the join table for faster lookups
CREATE INDEX idx_event_sponsors_event_id ON event_sponsors(event_id);
CREATE INDEX idx_event_sponsors_sponsor_id ON event_sponsors(sponsor_id);

COMMIT;
