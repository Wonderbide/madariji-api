-- Remove external authentication provider columns to follow hexagonal architecture
-- Users will be identified by email only, no provider-specific IDs in domain

-- Drop constraints first
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_supabase_user_id;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_auth0_user_id_key;

-- Drop indexes
DROP INDEX IF EXISTS idx_users_supabase_user_id;
DROP INDEX IF EXISTS idx_users_auth0_user_id;

-- Drop columns
ALTER TABLE users DROP COLUMN IF EXISTS auth0_user_id;
ALTER TABLE users DROP COLUMN IF EXISTS supabase_user_id;

-- Ensure email is unique and not null for authentication
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- Drop existing constraint if it exists (to handle re-runs)
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_email;

-- Add unique constraint
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Add index on email for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Comment
COMMENT ON COLUMN users.email IS 'Primary identifier for authentication - provider agnostic';