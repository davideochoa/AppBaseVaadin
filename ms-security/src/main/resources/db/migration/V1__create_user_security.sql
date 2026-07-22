CREATE TABLE user_security (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
