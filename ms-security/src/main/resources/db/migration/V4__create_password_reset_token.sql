CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    security_user_id BIGINT NOT NULL REFERENCES user_security(id),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_token_security_user_id ON password_reset_token(security_user_id);
