ALTER TABLE users DROP COLUMN IF EXISTS password_hash;
ALTER TABLE platform_admins DROP COLUMN IF EXISTS password_hash;
