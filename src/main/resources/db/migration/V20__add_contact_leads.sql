CREATE TABLE candidate_contact_leads (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    linkedin_url VARCHAR(500),
    position_id BIGINT NOT NULL REFERENCES positions(id),
    pipeline_id BIGINT NOT NULL REFERENCES recruitment_pipelines(id),
    status VARCHAR(30) NOT NULL,
    contact_channel VARCHAR(30),
    rejection_reason VARCHAR(50),
    note TEXT,
    candidate_process_id BIGINT REFERENCES candidate_processes(id),
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_contact_leads_company_status_created
    ON candidate_contact_leads(company_id, status, created_at DESC);
CREATE INDEX idx_contact_leads_position ON candidate_contact_leads(position_id);
CREATE UNIQUE INDEX uk_active_contact_lead_linkedin_position
    ON candidate_contact_leads(company_id, linkedin_url, position_id)
    WHERE active = TRUE AND status = 'CONTACTING' AND linkedin_url IS NOT NULL;
