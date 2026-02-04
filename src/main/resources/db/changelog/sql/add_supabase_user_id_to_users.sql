-- Add supabase_user_id column to users table
-- This column stores the Supabase Auth user ID as a string

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS supabase_user_id VARCHAR(255) UNIQUE;

-- Create index for fast lookups
CREATE INDEX IF NOT EXISTS idx_users_supabase_user_id 
ON users(supabase_user_id);

-- Comment
COMMENT ON COLUMN users.supabase_user_id IS 'Supabase Auth user ID for JWT authentication';