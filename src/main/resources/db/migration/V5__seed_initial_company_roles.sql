INSERT INTO roles (
    company_id, code, name, description, data_scope, system_role,
    created_at, updated_at, version, active
)
SELECT
    company.id,
    role.code,
    role.name,
    role.description,
    role.data_scope,
    TRUE,
    NOW(), NOW(), 0, TRUE
FROM companies company
CROSS JOIN (
    VALUES
        ('COMPANY_ADMIN', 'Şirket Yöneticisi', 'Şirket genelindeki işe alım süreçlerini görüntüler.', 'COMPANY'),
        ('HR', 'İnsan Kaynakları', 'Şirketin işe alım operasyonlarını yönetir.', 'COMPANY'),
        ('GENERAL_MANAGER', 'Genel Müdür', 'Şirket genelindeki süreçleri görüntüler.', 'COMPANY'),
        ('DEPARTMENT_MANAGER', 'Departman Yöneticisi', 'Atandığı departmanların süreçlerini görüntüler.', 'DEPARTMENT'),
        ('INTERVIEWER', 'Görüşmeci', 'Atandığı görüşmeleri görüntüler ve değerlendirir.', 'ASSIGNED')
) AS role(code, name, description, data_scope);

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles role
JOIN permissions permission ON (
    role.code = 'HR'
    AND permission.code IN (
        'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DEACTIVATE', 'USER_ROLE_ASSIGN',
        'DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_DEACTIVATE',
        'POSITION_VIEW', 'POSITION_CREATE', 'POSITION_UPDATE', 'POSITION_STATUS_CHANGE',
        'CANDIDATE_VIEW', 'CANDIDATE_CREATE', 'CANDIDATE_UPDATE',
        'CANDIDATE_PROCESS_VIEW', 'CANDIDATE_PROCESS_CREATE', 'CANDIDATE_STAGE_CHANGE',
        'CANDIDATE_COMPENSATION_VIEW', 'CANDIDATE_COMPENSATION_UPDATE',
        'INTERVIEW_VIEW', 'INTERVIEW_CREATE', 'INTERVIEW_EVALUATE',
        'PIPELINE_VIEW'
    )
) OR (
    role.code IN ('COMPANY_ADMIN', 'GENERAL_MANAGER')
    AND permission.code IN (
        'DEPARTMENT_VIEW', 'POSITION_VIEW', 'CANDIDATE_VIEW',
        'CANDIDATE_PROCESS_VIEW', 'CANDIDATE_COMPENSATION_VIEW',
        'INTERVIEW_VIEW', 'PIPELINE_VIEW'
    )
) OR (
    role.code = 'DEPARTMENT_MANAGER'
    AND permission.code IN (
        'DEPARTMENT_VIEW', 'POSITION_VIEW', 'CANDIDATE_VIEW',
        'CANDIDATE_PROCESS_VIEW', 'CANDIDATE_COMPENSATION_VIEW',
        'INTERVIEW_VIEW', 'PIPELINE_VIEW'
    )
) OR (
    role.code = 'INTERVIEWER'
    AND permission.code IN (
        'CANDIDATE_VIEW', 'CANDIDATE_PROCESS_VIEW',
        'INTERVIEW_VIEW', 'INTERVIEW_EVALUATE'
    )
);
