CREATE TABLE candidate_cvs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_candidate_cvs_candidate UNIQUE (candidate_id),
    CONSTRAINT uk_candidate_cvs_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_candidate_cvs_file_size CHECK (file_size > 0 AND file_size <= 5242880)
);

CREATE INDEX idx_candidate_cvs_company ON candidate_cvs(company_id);
