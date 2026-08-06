CREATE TABLE candidate_follow_ups (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    candidate_process_id BIGINT REFERENCES candidate_processes(id),
    assigned_to_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    due_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);
CREATE INDEX idx_follow_ups_candidate ON candidate_follow_ups(candidate_id);
CREATE INDEX idx_follow_ups_process ON candidate_follow_ups(candidate_process_id);
CREATE INDEX idx_follow_ups_assigned_to ON candidate_follow_ups(assigned_to_id);
CREATE INDEX idx_follow_ups_due_at ON candidate_follow_ups(due_at);
