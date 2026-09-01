CREATE TABLE IF NOT EXISTS video (
    id BIGSERIAL PRIMARY KEY,
    v_name VARCHAR(255) NOT NULL,
    v_type VARCHAR(50) NOT NULL,
    v_rank VARCHAR(20) NOT NULL,
    v_author VARCHAR(255),
    v_tag TEXT,
    v_series VARCHAR(255),
    v_season VARCHAR(50),
    v_number VARCHAR(50),
    v_file TEXT NOT NULL DEFAULT '/videos'
);

ALTER TABLE IF EXISTS video
    ADD COLUMN IF NOT EXISTS v_series VARCHAR(255),
    ADD COLUMN IF NOT EXISTS v_season VARCHAR(50),
    ADD COLUMN IF NOT EXISTS v_number VARCHAR(50);
