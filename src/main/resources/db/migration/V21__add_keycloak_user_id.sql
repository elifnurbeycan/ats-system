ALTER TABLE users ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(100);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_keycloak_user_id ON users(keycloak_user_id)
    WHERE keycloak_user_id IS NOT NULL;
