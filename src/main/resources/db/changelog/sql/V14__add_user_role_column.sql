-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_FREE';

-- Add index for role queries
CREATE INDEX idx_users_role ON users(role);

-- Update existing users to have ROLE_FREE (redundant but safe)
UPDATE users SET role = 'ROLE_FREE' WHERE role IS NULL;

-- Add comment
COMMENT ON COLUMN users.role IS 'User role: ROLE_FREE, ROLE_PREMIUM, or ROLE_ADMIN';