CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    actor_user_id BIGINT,
    actor_reference VARCHAR(200) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_reference VARCHAR(500),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(1000) NOT NULL,
    request_data TEXT,
    response_data TEXT,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    request_id VARCHAR(100) NOT NULL,
    http_status INTEGER NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_company_time ON audit_logs(company_id, occurred_at DESC);
CREATE INDEX idx_audit_logs_company_actor ON audit_logs(company_id, actor_user_id);
CREATE INDEX idx_audit_logs_company_resource ON audit_logs(company_id, resource_type);
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);

INSERT INTO permissions (code, name, description, category, system_permission, display_order,
                         created_at, updated_at, version, active)
VALUES ('AUDIT_VIEW', 'Audit Kayitlarini Goruntuleme',
        'Sirket icindeki basarili veri degisikliklerinin denetim kayitlarini goruntuler.',
        'AUDIT', TRUE, 270, NOW(), NOW(), 0, TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles role
JOIN permissions permission ON permission.code = 'AUDIT_VIEW'
WHERE role.code IN ('HR', 'COMPANY_ADMIN') AND role.active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
