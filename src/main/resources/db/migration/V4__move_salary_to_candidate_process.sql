ALTER TABLE candidate_processes
    ADD COLUMN current_salary NUMERIC(19,2);

UPDATE candidate_processes candidate_process
SET
    current_salary = candidate.current_salary,
    salary_currency = COALESCE(
            candidate_process.salary_currency,
            candidate.salary_currency
    )
FROM candidates candidate
WHERE candidate.id = candidate_process.candidate_id;

ALTER TABLE candidates
    DROP COLUMN current_salary,
    DROP COLUMN salary_currency;
