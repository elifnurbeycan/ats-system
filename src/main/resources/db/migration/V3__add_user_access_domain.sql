CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    system_permission BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE INDEX idx_permissions_category ON permissions(category);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    data_scope VARCHAR(30) NOT NULL,
    system_role BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_roles_company_code UNIQUE (company_id, code)
);

CREATE INDEX idx_roles_company ON roles(company_id);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permissions_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    department_id BIGINT REFERENCES departments(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    must_change_password BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT uk_users_company_email UNIQUE (company_id, email)
);

CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_department ON users(department_id);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE department_manager_assignments (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    deactivated_at TIMESTAMPTZ
);

CREATE INDEX idx_department_manager_assignments_department
    ON department_manager_assignments(department_id);

CREATE INDEX idx_department_manager_assignments_user
    ON department_manager_assignments(user_id);

INSERT INTO permissions (
    code, name, category, system_permission, display_order,
    created_at, updated_at, version, active
)
SELECT
    permission.code,
    permission.name,
    permission.category,
    TRUE,
    permission.display_order,
    NOW(), NOW(), 0, TRUE
FROM (
    VALUES
        ('USER_VIEW', 'Kullanıcı Görüntüleme', 'USER', 10),
        ('USER_CREATE', 'Kullanıcı Oluşturma', 'USER', 20),
        ('USER_UPDATE', 'Kullanıcı Güncelleme', 'USER', 30),
        ('USER_DEACTIVATE', 'Kullanıcı Pasifleştirme', 'USER', 40),
        ('USER_ROLE_ASSIGN', 'Kullanıcı Rolü Atama', 'USER', 50),
        ('DEPARTMENT_VIEW', 'Departman Görüntüleme', 'DEPARTMENT', 60),
        ('DEPARTMENT_CREATE', 'Departman Oluşturma', 'DEPARTMENT', 70),
        ('DEPARTMENT_UPDATE', 'Departman Güncelleme', 'DEPARTMENT', 80),
        ('DEPARTMENT_DEACTIVATE', 'Departman Pasifleştirme', 'DEPARTMENT', 90),
        ('POSITION_VIEW', 'Pozisyon Görüntüleme', 'POSITION', 100),
        ('POSITION_CREATE', 'Pozisyon Oluşturma', 'POSITION', 110),
        ('POSITION_UPDATE', 'Pozisyon Güncelleme', 'POSITION', 120),
        ('POSITION_STATUS_CHANGE', 'Pozisyon Durumu Değiştirme', 'POSITION', 130),
        ('CANDIDATE_VIEW', 'Aday Görüntüleme', 'CANDIDATE', 140),
        ('CANDIDATE_CREATE', 'Aday Oluşturma', 'CANDIDATE', 150),
        ('CANDIDATE_UPDATE', 'Aday Güncelleme', 'CANDIDATE', 160),
        ('CANDIDATE_PROCESS_VIEW', 'Aday Süreci Görüntüleme', 'CANDIDATE_PROCESS', 170),
        ('CANDIDATE_PROCESS_CREATE', 'Aday Süreci Oluşturma', 'CANDIDATE_PROCESS', 180),
        ('CANDIDATE_STAGE_CHANGE', 'Aday Aşaması Değiştirme', 'CANDIDATE_PROCESS', 190),
        ('CANDIDATE_COMPENSATION_VIEW', 'Maaş Bilgisi Görüntüleme', 'COMPENSATION', 200),
        ('CANDIDATE_COMPENSATION_UPDATE', 'Maaş Bilgisi Güncelleme', 'COMPENSATION', 210),
        ('INTERVIEW_VIEW', 'Görüşme Görüntüleme', 'INTERVIEW', 220),
        ('INTERVIEW_CREATE', 'Görüşme Oluşturma', 'INTERVIEW', 230),
        ('INTERVIEW_EVALUATE', 'Görüşme Değerlendirme', 'INTERVIEW', 240),
        ('PIPELINE_VIEW', 'Pipeline Görüntüleme', 'PIPELINE', 250),
        ('PIPELINE_MANAGE', 'Pipeline Yönetme', 'PIPELINE', 260)
) AS permission(code, name, category, display_order);
