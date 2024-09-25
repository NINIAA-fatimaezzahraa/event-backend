-- event-db.update users definition

ALTER TABLE IF EXISTS users
    DROP COLUMN username,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN last_login_date TIMESTAMP;

COMMIT;
