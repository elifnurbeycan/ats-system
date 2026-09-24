INSERT INTO permissions (code, name, description, category, system_permission, display_order,
                         created_at, updated_at, version, active)
VALUES
    ('CONTACT_LEAD_VIEW', 'İletişim kayıtlarını görüntüleme', 'İletişim havuzundaki kayıtları ve sonuçlarını görüntüleme', 'CONTACT_LEAD', TRUE, 330, NOW(), NOW(), 0, TRUE),
    ('CONTACT_LEAD_CREATE', 'İletişim kaydı oluşturma', 'Yeni kişileri iletişim havuzuna ekleme', 'CONTACT_LEAD', TRUE, 340, NOW(), NOW(), 0, TRUE),
    ('CONTACT_LEAD_UPDATE', 'İletişim kaydını düzenleme', 'İletişim kayıtlarının temel bilgilerini düzenleme', 'CONTACT_LEAD', TRUE, 350, NOW(), NOW(), 0, TRUE),
    ('CONTACT_LEAD_RESOLVE', 'İletişim sonucunu yönetme', 'İletişim sonucunu, kanalını, notunu ve ret nedenini kaydetme', 'CONTACT_LEAD', TRUE, 360, NOW(), NOW(), 0, TRUE)
ON CONFLICT (code) DO NOTHING;

-- Mevcut sistem rollerinin iletişim ekranı çalışmaya devam eder.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('CONTACT_LEAD_VIEW', 'CONTACT_LEAD_CREATE', 'CONTACT_LEAD_UPDATE', 'CONTACT_LEAD_RESOLVE')
WHERE r.code IN ('COMPANY_ADMIN', 'HR', 'RECRUITER') AND r.system_role = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
