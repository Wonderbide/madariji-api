-- Alter the existing supabase_user_id column from UUID to VARCHAR(255)
-- This is needed because the Java model expects a String, not UUID

-- First drop the index if it exists
DROP INDEX IF EXISTS idx_users_supabase_user_id;

-- Alter the column type from UUID to VARCHAR(255)
-- We need to cast the existing UUID values to text
ALTER TABLE users 
ALTER COLUMN supabase_user_id TYPE VARCHAR(255) 
USING supabase_user_id::text;

-- Ensure the column remains unique
ALTER TABLE users 
ADD CONSTRAINT uk_users_supabase_user_id UNIQUE (supabase_user_id);

-- Recreate the index
CREATE INDEX idx_users_supabase_user_id ON users(supabase_user_id);

-- Update comment
COMMENT ON COLUMN users.supabase_user_id IS 'Supabase Auth user ID stored as string';