ALTER TABLE candidate_notes
    ADD COLUMN IF NOT EXISTS entry_type VARCHAR(20) NOT NULL DEFAULT 'NOTE';

CREATE INDEX IF NOT EXISTS idx_candidate_notes_entry_type
    ON candidate_notes(company_id, candidate_id, entry_type, created_at DESC);

INSERT INTO permissions (code, name, description, category, system_permission, display_order,
                         created_at, updated_at, version, active)
VALUES
    ('CANDIDATE_NOTE_VIEW', 'Aday Notlarını Görüntüleme', 'Aday notlarını görüntüleme', 'CANDIDATE_NOTE',  TRUE, 270, NOW(), NOW(), 0, TRUE),
    ('CANDIDATE_NOTE_CREATE', 'Aday Notu Ekleme', 'Adaylara not ekleme', 'CANDIDATE_NOTE', TRUE, 280, NOW(), NOW(), 0, TRUE),
    ('CANDIDATE_NOTE_UPDATE', 'Aday Notu Düzenleme', 'Aday notlarını güncelleme ve pasifleştirme', 'CANDIDATE_NOTE', TRUE, 290, NOW(), NOW(), 0, TRUE),
    ('CANDIDATE_EVALUATION_VIEW', 'Aday Değerlendirmelerini Görüntüleme', 'Aday değerlendirmelerini görüntüleme', 'CANDIDATE_EVALUATION', TRUE, 300, NOW(), NOW(), 0, TRUE),
    ('CANDIDATE_EVALUATION_CREATE', 'Aday Değerlendirmesi Ekleme', 'Adaylar için ekip değerlendirmesi ekleme', 'CANDIDATE_EVALUATION', TRUE, 310, NOW(), NOW(), 0, TRUE),
    ('CANDIDATE_EVALUATION_UPDATE', 'Aday Değerlendirmesi Düzenleme', 'Aday değerlendirmelerini güncelleme', 'CANDIDATE_EVALUATION', TRUE, 320, NOW(), NOW(), 0, TRUE)
ON CONFLICT (code) DO NOTHING;
