CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_companies_code UNIQUE (code)
);

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_departments_company_code UNIQUE (company_id, code)
);

CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    title VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    vacancy_count INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    opened_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_positions_company_code UNIQUE (company_id, code)
);

CREATE TABLE recruitment_pipelines (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    default_pipeline BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_recruitment_pipelines_company_code UNIQUE (company_id, code)
);

CREATE TABLE pipeline_stages (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    pipeline_id BIGINT NOT NULL REFERENCES recruitment_pipelines(id),
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER NOT NULL,
    stage_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_pipeline_stages_pipeline_code UNIQUE (pipeline_id, code),
    CONSTRAINT uk_pipeline_stages_pipeline_order UNIQUE (pipeline_id, display_order)
);

CREATE TABLE candidates (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    linkedin_url VARCHAR(500),
    email VARCHAR(255),
    phone VARCHAR(30),
    city VARCHAR(100),
    current_company VARCHAR(150),
    current_job_title VARCHAR(150),
    current_salary NUMERIC(19,2),
    salary_currency VARCHAR(3),
    notice_period_days INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_candidates_company_linkedin_url UNIQUE (company_id, linkedin_url)
);

CREATE TABLE candidate_processes (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    position_id BIGINT NOT NULL REFERENCES positions(id),
    pipeline_id BIGINT NOT NULL REFERENCES recruitment_pipelines(id),
    current_stage_id BIGINT NOT NULL REFERENCES pipeline_stages(id),
    expected_salary NUMERIC(19,2),
    offered_salary NUMERIC(19,2),
    salary_currency VARCHAR(3),
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_candidate_processes_company ON candidate_processes(company_id);
CREATE INDEX idx_candidate_processes_position ON candidate_processes(position_id);
CREATE INDEX idx_candidate_processes_current_stage ON candidate_processes(current_stage_id);

CREATE TABLE candidate_process_stage_history (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    candidate_process_id BIGINT NOT NULL REFERENCES candidate_processes(id),
    from_stage_id BIGINT REFERENCES pipeline_stages(id),
    to_stage_id BIGINT NOT NULL REFERENCES pipeline_stages(id),
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_stage_history_candidate_process
    ON candidate_process_stage_history(candidate_process_id);

CREATE INDEX idx_stage_history_to_stage
    ON candidate_process_stage_history(to_stage_id);
