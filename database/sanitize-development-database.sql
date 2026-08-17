-- Run only against a disposable restored copy of the development database.
-- Never run this script against the live local development database.

BEGIN;

TRUNCATE TABLE auth_refresh_tokens RESTART IDENTITY CASCADE;
TRUNCATE TABLE platform_refresh_tokens RESTART IDENTITY CASCADE;
TRUNCATE TABLE audit_logs RESTART IDENTITY;
TRUNCATE TABLE candidate_cvs RESTART IDENTITY;

UPDATE candidates
SET first_name = 'Demo',
    last_name = 'Aday ' || id,
    linkedin_url = 'https://www.linkedin.com/in/demo-candidate-' || id,
    email = 'candidate' || id || '@example.com',
    phone = NULL,
    city = 'İstanbul',
    current_company = 'Demo Şirket',
    current_job_title = 'Demo Pozisyon';

UPDATE candidate_contact_leads
SET first_name = 'Demo',
    last_name = 'İletişim ' || id,
    linkedin_url = 'https://www.linkedin.com/in/demo-contact-' || id,
    note = CASE WHEN note IS NULL THEN NULL ELSE 'Anonimleştirilmiş iletişim notu' END;

UPDATE candidate_notes
SET content = 'Anonimleştirilmiş aday notu';

UPDATE candidate_interactions
SET subject = CASE WHEN subject IS NULL THEN NULL ELSE 'Demo iletişim' END,
    summary = 'Anonimleştirilmiş iletişim özeti';

UPDATE candidate_follow_ups
SET title = 'Demo takip kaydı',
    description = CASE WHEN description IS NULL THEN NULL ELSE 'Anonimleştirilmiş takip açıklaması' END;

COMMIT;
