ALTER TABLE candidate_notes
    ADD COLUMN IF NOT EXISTS pipeline_stage_id BIGINT;

ALTER TABLE candidate_notes
    ADD CONSTRAINT fk_candidate_notes_pipeline_stage
        FOREIGN KEY (pipeline_stage_id) REFERENCES pipeline_stages(id);

UPDATE candidate_notes note
SET pipeline_stage_id = process.current_stage_id
FROM candidate_processes process
WHERE note.candidate_process_id = process.id
  AND note.entry_type = 'EVALUATION'
  AND note.pipeline_stage_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_candidate_notes_pipeline_stage
    ON candidate_notes(pipeline_stage_id);
