CREATE TABLE IF NOT EXISTS video (
    id BIGSERIAL PRIMARY KEY,
    v_name VARCHAR(255) NOT NULL,
    v_type VARCHAR(50) NOT NULL,
    v_rank VARCHAR(20) NOT NULL,
    v_author VARCHAR(255),
    v_tag TEXT
);
