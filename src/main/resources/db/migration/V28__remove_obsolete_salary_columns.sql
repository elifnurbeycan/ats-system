-- The application stores only the candidate's expected salary now.
ALTER TABLE candidate_processes
    DROP COLUMN IF EXISTS current_salary,
    DROP COLUMN IF EXISTS offered_salary;
