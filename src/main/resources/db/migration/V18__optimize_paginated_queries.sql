CREATE INDEX IF NOT EXISTS idx_departments_company_active_name
    ON departments(company_id, active, name);

CREATE INDEX IF NOT EXISTS idx_positions_company_active_department_status_title
    ON positions(company_id, active, department_id, status, title);

CREATE INDEX IF NOT EXISTS idx_pipelines_company_active_name
    ON recruitment_pipelines(company_id, active, name);

CREATE INDEX IF NOT EXISTS idx_companies_name
    ON companies(name);

CREATE INDEX IF NOT EXISTS idx_manager_assignments_company_department_active_started
    ON department_manager_assignments(company_id, department_id, active, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_candidate_processes_company_candidate_active
    ON candidate_processes(company_id, candidate_id, active);

CREATE INDEX IF NOT EXISTS idx_stage_history_company_process_created
    ON candidate_process_stage_history(company_id, candidate_process_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_candidate_notes_company_candidate_active_created
    ON candidate_notes(company_id, candidate_id, active, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_interactions_company_candidate_active_occurred
    ON candidate_interactions(company_id, candidate_id, active, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_interviews_company_process_active_created
    ON interviews(company_id, candidate_process_id, active, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_followups_company_candidate_active_created
    ON candidate_follow_ups(company_id, candidate_id, active, created_at DESC);
