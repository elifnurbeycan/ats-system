-- Yeni modül izinleri mevcut şirketlerin şirket yöneticisi ve İK sistem rollerine verilir.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'CANDIDATE_NOTE_VIEW', 'CANDIDATE_NOTE_CREATE', 'CANDIDATE_NOTE_UPDATE',
    'CANDIDATE_EVALUATION_VIEW', 'CANDIDATE_EVALUATION_CREATE', 'CANDIDATE_EVALUATION_UPDATE'
)
WHERE r.code IN ('COMPANY_ADMIN', 'HR') AND r.system_role = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
