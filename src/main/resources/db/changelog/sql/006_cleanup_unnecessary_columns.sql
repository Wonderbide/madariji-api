-- Cleanup unnecessary columns from all three tables
-- This removes over-engineered and unused columns

-- 1. Clean up llm_models table
ALTER TABLE llm_models 
DROP COLUMN IF EXISTS temperature_override,
DROP COLUMN IF EXISTS max_tokens_override,
DROP COLUMN IF EXISTS top_p,
DROP COLUMN IF EXISTS frequency_penalty,
DROP COLUMN IF EXISTS presence_penalty,
DROP COLUMN IF EXISTS context_window,
DROP COLUMN IF EXISTS avg_response_time_ms,
DROP COLUMN IF EXISTS success_rate,
DROP COLUMN IF EXISTS performance_tier,
DROP COLUMN IF EXISTS supported_features;

-- 2. Clean up prompt_library table  
ALTER TABLE prompt_library
DROP COLUMN IF EXISTS is_deprecated,
DROP COLUMN IF EXISTS deprecated_at,
DROP COLUMN IF EXISTS deprecated_by,
DROP COLUMN IF EXISTS created_by,
DROP COLUMN IF EXISTS updated_by;

-- 3. Clean up ai_workflow_config table
ALTER TABLE ai_workflow_config
DROP COLUMN IF EXISTS prompt_id,
DROP COLUMN IF EXISTS description,
DROP COLUMN IF EXISTS configured_by,
DROP COLUMN IF EXISTS created_by,
DROP COLUMN IF EXISTS priority;

-- Log the cleanup
DO $$
BEGIN
    RAISE NOTICE 'Cleaned up unnecessary columns from llm_models, prompt_library, and ai_workflow_config tables';
END $$;