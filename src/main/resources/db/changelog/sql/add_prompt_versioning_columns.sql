-- Add versioning columns to prompt_template table
ALTER TABLE prompt_template 
ADD COLUMN IF NOT EXISTS is_deprecated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE prompt_template 
ADD COLUMN IF NOT EXISTS deprecated_at TIMESTAMP;

ALTER TABLE prompt_template 
ADD COLUMN IF NOT EXISTS deprecated_by VARCHAR(255);

-- Create prompt_ab_test table if it doesn't exist
CREATE TABLE IF NOT EXISTS prompt_ab_test (
    id UUID NOT NULL PRIMARY KEY,
    confidence_level FLOAT(53),
    created_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(255),
    description TEXT,
    end_date TIMESTAMP(6),
    min_sample_size INTEGER,
    primary_metric VARCHAR(50) CHECK (primary_metric IN ('SUCCESS_RATE','RESPONSE_TIME','COST_EFFICIENCY','QUALITY_SCORE','TOKEN_EFFICIENCY')),
    prompt_identifier VARCHAR(100) NOT NULL,
    results_summary TEXT,
    start_date TIMESTAMP(6),
    statistical_significance FLOAT(53),
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','ACTIVE','PAUSED','COMPLETED','CANCELLED','FAILED')),
    target_groups TEXT,
    test_name VARCHAR(255) NOT NULL,
    traffic_split_a INTEGER NOT NULL,
    traffic_split_b INTEGER NOT NULL,
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    version_a VARCHAR(20) NOT NULL,
    version_b VARCHAR(20) NOT NULL,
    winner VARCHAR(20)
);

-- Create indexes for prompt_ab_test
CREATE INDEX IF NOT EXISTS idx_ab_test_prompt_identifier ON prompt_ab_test (prompt_identifier);
CREATE INDEX IF NOT EXISTS idx_ab_test_status ON prompt_ab_test (status);
CREATE INDEX IF NOT EXISTS idx_ab_test_created_at ON prompt_ab_test (created_at);