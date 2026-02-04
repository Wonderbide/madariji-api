-- Final cleanup of llm_models table to match AI_CONFIG_MINIMAL.md specification

-- 1. Drop all unnecessary columns that aren't in the plan
ALTER TABLE llm_models 
DROP COLUMN IF EXISTS cost_per_1k_input_tokens,
DROP COLUMN IF EXISTS cost_per_1k_output_tokens,
DROP COLUMN IF EXISTS performance_tier,
DROP COLUMN IF EXISTS avg_response_time_ms,
DROP COLUMN IF EXISTS success_rate,
DROP COLUMN IF EXISTS supported_features,
DROP COLUMN IF EXISTS temperature_override,
DROP COLUMN IF EXISTS max_tokens_override,
DROP COLUMN IF EXISTS is_enabled,
DROP COLUMN IF EXISTS max_context_tokens;  -- Drop this since we already have context_window

-- 2. Add missing column from the plan
ALTER TABLE llm_models 
ADD COLUMN IF NOT EXISTS api_version VARCHAR(20);

-- 3. Verify final structure matches the plan:
-- Expected columns after this migration:
-- id, model_code, model_name, provider, temperature, max_output_tokens,
-- top_p, frequency_penalty, presence_penalty, context_window,
-- api_endpoint, api_version, created_at, updated_at

DO $$
BEGIN
    RAISE NOTICE 'llm_models table cleaned up to match AI_CONFIG_MINIMAL.md specification';
END $$;