INSERT INTO companies (
    name, code, status, created_at, updated_at, version, active
) VALUES (
    'Yaşar Bilgi', 'YASAR_BILGI', 'ACTIVE', NOW(), NOW(), 0, TRUE
);

INSERT INTO departments (
    company_id, name, code, description,
    created_at, updated_at, version, active
)
SELECT
    id, 'Yazılım', 'SOFTWARE', 'Yazılım geliştirme departmanı',
    NOW(), NOW(), 0, TRUE
FROM companies
WHERE code = 'YASAR_BILGI';

INSERT INTO positions (
    company_id, department_id, title, code, description,
    vacancy_count, status, opened_at,
    created_at, updated_at, version, active
)
SELECT
    company.id,
    department.id,
    'Java Developer',
    'JAVA_DEVELOPER',
    'Java Developer işe alım pozisyonu',
    1,
    'OPEN',
    NOW(),
    NOW(), NOW(), 0, TRUE
FROM companies company
JOIN departments department ON department.company_id = company.id
WHERE company.code = 'YASAR_BILGI'
  AND department.code = 'SOFTWARE';

INSERT INTO recruitment_pipelines (
    company_id, name, code, description, default_pipeline,
    created_at, updated_at, version, active
)
SELECT
    id,
    'Standart İşe Alım Süreci',
    'STANDARD_RECRUITMENT',
    'Yaşar Bilgi için başlangıç işe alım süreci',
    TRUE,
    NOW(), NOW(), 0, TRUE
FROM companies
WHERE code = 'YASAR_BILGI';

INSERT INTO pipeline_stages (
    company_id, pipeline_id, name, code, description,
    display_order, stage_type,
    created_at, updated_at, version, active
)
SELECT
    company.id,
    pipeline.id,
    stage.name,
    stage.code,
    stage.description,
    stage.display_order,
    stage.stage_type,
    NOW(), NOW(), 0, TRUE
FROM companies company
JOIN recruitment_pipelines pipeline ON pipeline.company_id = company.id
CROSS JOIN (
    VALUES
        ('İletişime Geçildi / Yanıt Bekleniyor', 'WAITING_RESPONSE', 'Adaydan ilk iletişim sonrası yanıt bekleniyor.', 1, 'ACTIVE'),
        ('Olumlu Dönüş / Bilgi Tamamlama', 'INFORMATION_COLLECTION', 'Adayın mevcut bilgileri isteğe bağlı olarak tamamlanıyor.', 2, 'ACTIVE'),
        ('İK Görüşmesi', 'HR_INTERVIEW', 'Aday İK görüşmesi aşamasında.', 3, 'ACTIVE'),
        ('Yönetici Değerlendirmesi', 'MANAGER_REVIEW', 'Aday ilgili yönetici tarafından değerlendiriliyor.', 4, 'ACTIVE'),
        ('Teknik Görüşme', 'TECHNICAL_INTERVIEW', 'Aday teknik olarak değerlendiriliyor.', 5, 'ACTIVE'),
        ('Teklif', 'OFFER', 'Aday için teklif süreci yürütülüyor.', 6, 'ACTIVE'),
        ('İşe Alındı', 'HIRED', 'Aday süreci başarıyla tamamlandı.', 7, 'HIRED'),
        ('Beklemede', 'ON_HOLD', 'Aday süreci geçici olarak bekletiliyor.', 8, 'ON_HOLD'),
        ('Reddedildi', 'REJECTED', 'Aday süreci olumsuz tamamlandı.', 9, 'REJECTED')
) AS stage(name, code, description, display_order, stage_type)
WHERE company.code = 'YASAR_BILGI'
  AND pipeline.code = 'STANDARD_RECRUITMENT';
