-- İlk temas bir değerlendirme aşaması değildir. Mevcut süreçleri pipeline'ın
-- bir sonraki aktif aşamasına taşıyıp WAITING_RESPONSE aşamasını arşivler.
WITH contact_stages AS (
    SELECT contact.id AS from_stage_id,
           next_stage.id AS to_stage_id
    FROM pipeline_stages contact
    JOIN LATERAL (
        SELECT candidate.id
        FROM pipeline_stages candidate
        WHERE candidate.pipeline_id = contact.pipeline_id
          AND candidate.active = TRUE
          AND candidate.stage_type = 'ACTIVE'
          AND candidate.display_order > contact.display_order
        ORDER BY candidate.display_order
        LIMIT 1
    ) next_stage ON TRUE
    WHERE contact.code = 'WAITING_RESPONSE'
      AND contact.active = TRUE
), moved_processes AS (
    UPDATE candidate_processes process
       SET current_stage_id = contact.to_stage_id,
           updated_at = NOW(),
           version = process.version + 1
      FROM contact_stages contact
     WHERE process.current_stage_id = contact.from_stage_id
       AND process.active = TRUE
    RETURNING process.id, process.company_id, contact.from_stage_id, contact.to_stage_id,
              process.updated_by
)
INSERT INTO candidate_process_stage_history (
    company_id, candidate_process_id, from_stage_id, to_stage_id, reason,
    created_at, updated_at, created_by, updated_by, version, active
)
SELECT company_id, id, from_stage_id, to_stage_id,
       'İlk iletişim kaydı pipeline dışındaki İletişim modülüne taşındı.',
       NOW(), NOW(), updated_by, updated_by, 0, TRUE
FROM moved_processes;

-- Önce sıra çakışmasını engellemek için kaldırılan aşamayı geçici negatif sıraya al.
UPDATE pipeline_stages
   SET display_order = -id::INTEGER,
       active = FALSE,
       deactivated_at = NOW(),
       updated_at = NOW(),
       version = version + 1
 WHERE code = 'WAITING_RESPONSE'
   AND active = TRUE;

-- Kalan aşamaları yeniden 1'den başlayan kesintisiz sıraya getir.
WITH ordered AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY pipeline_id ORDER BY display_order, id)::INTEGER AS new_order
    FROM pipeline_stages
    WHERE active = TRUE
)
UPDATE pipeline_stages stage
   SET display_order = ordered.new_order,
       updated_at = NOW(),
       version = stage.version + 1
  FROM ordered
 WHERE stage.id = ordered.id
   AND stage.display_order <> ordered.new_order;
