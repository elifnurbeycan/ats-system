CREATE TABLE candidate_interactions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    candidate_process_id BIGINT REFERENCES candidate_processes(id),
    channel VARCHAR(30) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    subject VARCHAR(200),
    summary TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_interactions_company
    ON candidate_interactions(company_id);

CREATE INDEX idx_interactions_candidate
    ON candidate_interactions(candidate_id);

CREATE INDEX idx_interactions_process
    ON candidate_interactions(candidate_process_id);

CREATE INDEX idx_interactions_occurred_at
    ON candidate_interactions(occurred_at);
