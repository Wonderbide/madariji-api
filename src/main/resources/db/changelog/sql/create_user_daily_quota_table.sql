-- Create user_daily_quota table for freemium rate limiting
CREATE TABLE IF NOT EXISTS user_daily_quota (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quota_date DATE NOT NULL DEFAULT CURRENT_DATE,
    word_consultations INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- One entry per user per day
    CONSTRAINT uk_user_daily_quota_user_date UNIQUE (user_id, quota_date)
);

-- Index for fast lookups
CREATE INDEX IF NOT EXISTS idx_user_daily_quota_user_date ON user_daily_quota(user_id, quota_date);

-- Comment
COMMENT ON TABLE user_daily_quota IS 'Tracks daily usage quotas for freemium users';
COMMENT ON COLUMN user_daily_quota.word_consultations IS 'Number of word consultations today';
