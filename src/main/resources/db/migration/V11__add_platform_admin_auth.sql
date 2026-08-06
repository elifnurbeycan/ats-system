CREATE TABLE platform_admins (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_platform_admins_email UNIQUE (email)
);

CREATE TABLE platform_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    platform_admin_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_platform_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_platform_refresh_tokens_admin FOREIGN KEY (platform_admin_id)
        REFERENCES platform_admins (id)
);

CREATE INDEX idx_platform_refresh_tokens_admin ON platform_refresh_tokens (platform_admin_id);
CREATE INDEX idx_platform_refresh_tokens_expires_at ON platform_refresh_tokens (expires_at);
