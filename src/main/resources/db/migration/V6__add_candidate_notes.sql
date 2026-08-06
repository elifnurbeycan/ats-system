CREATE TABLE candidate_notes (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    candidate_process_id BIGINT REFERENCES candidate_processes(id),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_candidate_notes_company
    ON candidate_notes(company_id);

CREATE INDEX idx_candidate_notes_candidate
    ON candidate_notes(candidate_id);

CREATE INDEX idx_candidate_notes_process
    ON candidate_notes(candidate_process_id);
