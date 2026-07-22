CREATE TABLE audit_event (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    email VARCHAR(150),
    ip_address VARCHAR(45),
    occurred_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_event_type ON audit_event(type);
CREATE INDEX idx_audit_event_email ON audit_event(email);
