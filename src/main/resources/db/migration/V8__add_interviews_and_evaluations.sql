CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_process_id BIGINT NOT NULL REFERENCES candidate_processes(id),
    type VARCHAR(30) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    duration_minutes INTEGER NOT NULL,
    location VARCHAR(300),
    meeting_url VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT, updated_by BIGINT, version BIGINT NOT NULL,
    active BOOLEAN NOT NULL, deactivated_at TIMESTAMPTZ
);
CREATE INDEX idx_interviews_process ON interviews(candidate_process_id);
CREATE INDEX idx_interviews_scheduled_at ON interviews(scheduled_at);

CREATE TABLE interview_interviewers (
    interview_id BIGINT NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    CONSTRAINT uk_interview_interviewers UNIQUE (interview_id, user_id)
);

CREATE TABLE interview_evaluations (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    interview_id BIGINT NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    evaluator_id BIGINT NOT NULL REFERENCES users(id),
    score INTEGER NOT NULL,
    recommendation VARCHAR(30) NOT NULL,
    feedback TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT, updated_by BIGINT, version BIGINT NOT NULL,
    active BOOLEAN NOT NULL, deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_interview_evaluator UNIQUE (interview_id, evaluator_id),
    CONSTRAINT ck_interview_evaluation_score CHECK (score BETWEEN 1 AND 5)
);
CREATE INDEX idx_interview_evaluations_interview ON interview_evaluations(interview_id);
