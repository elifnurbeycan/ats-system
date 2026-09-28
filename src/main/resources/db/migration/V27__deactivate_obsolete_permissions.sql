UPDATE permissions
SET active = FALSE,
    deactivated_at = COALESCE(deactivated_at, NOW()),
    updated_at = NOW(),
    version = version + 1
WHERE code IN ('CANDIDATE_CREATE', 'CONTACT_LEAD_UPDATE')
  AND active = TRUE;
