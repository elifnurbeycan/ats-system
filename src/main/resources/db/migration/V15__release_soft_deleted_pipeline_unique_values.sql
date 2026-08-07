UPDATE recruitment_pipelines
SET code = LEFT(code, GREATEST(0, 50 - LENGTH('__ARCHIVED_' || id::text)))
           || '__ARCHIVED_' || id::text
WHERE active = FALSE
  AND code NOT LIKE '%__ARCHIVED_' || id::text;

UPDATE pipeline_stages
SET code = LEFT(code, GREATEST(0, 50 - LENGTH('__ARCHIVED_' || id::text)))
           || '__ARCHIVED_' || id::text,
    display_order = -id
WHERE active = FALSE
  AND code NOT LIKE '%__ARCHIVED_' || id::text;
