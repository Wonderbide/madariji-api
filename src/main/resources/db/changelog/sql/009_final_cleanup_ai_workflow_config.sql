-- Final cleanup of ai_workflow_config table to match AI_CONFIG_MINIMAL.md specification

-- According to AI_CONFIG_MINIMAL.md, ai_workflow_config should have:
-- id, workflow_type, prompt_id, model_id, is_active, description, created_at, updated_at, configured_by

-- Current state: we have workflow_type, model_id (as VARCHAR), is_active
-- Need to: add prompt_id as UUID FK, keep description and configured_by, remove other columns

-- 1. Remove unnecessary columns
ALTER TABLE ai_workflow_config 
DROP COLUMN IF EXISTS provider,      -- Not needed, provider is in llm_models
DROP COLUMN IF EXISTS priority,      -- Already dropped
DROP COLUMN IF EXISTS created_by;   -- Use configured_by instead

-- 2. Fix model_id to be UUID FK instead of VARCHAR
-- First add a new column for UUID reference
ALTER TABLE ai_workflow_config 
ADD COLUMN IF NOT EXISTS model_id_uuid UUID;

-- Update with actual UUIDs from llm_models
UPDATE ai_workflow_config awc
SET model_id_uuid = (
    SELECT lm.id 
    FROM llm_models lm 
    WHERE lm.model_code = awc.model_id
    LIMIT 1
)
WHERE model_id_uuid IS NULL;

-- Drop old VARCHAR column and rename new one
ALTER TABLE ai_workflow_config 
DROP COLUMN model_id;

ALTER TABLE ai_workflow_config 
RENAME COLUMN model_id_uuid TO model_id;

-- 3. Ensure prompt_id exists (should already be there from plan)
-- prompt_id already exists, just needs to be populated

-- 4. Add foreign key constraints
ALTER TABLE ai_workflow_config 
ADD CONSTRAINT fk_workflow_prompt FOREIGN KEY (prompt_id) REFERENCES prompt_library(id),
ADD CONSTRAINT fk_workflow_model FOREIGN KEY (model_id) REFERENCES llm_models(id);

-- 5. Verify final structure matches the plan:
-- Expected columns after this migration:
-- id, workflow_type, prompt_id, model_id, is_active, description, created_at, updated_at, configured_by

DO $$
BEGIN
    RAISE NOTICE 'ai_workflow_config table cleaned up to match AI_CONFIG_MINIMAL.md specification';
END $$;