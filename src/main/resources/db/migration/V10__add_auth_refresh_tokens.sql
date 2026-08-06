CREATE TABLE auth_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT, updated_by BIGINT, version BIGINT NOT NULL,
    active BOOLEAN NOT NULL, deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
);
CREATE INDEX idx_refresh_tokens_user ON auth_refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON auth_refresh_tokens(expires_at);
