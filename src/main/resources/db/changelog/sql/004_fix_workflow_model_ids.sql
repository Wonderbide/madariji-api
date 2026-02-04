-- Fix ai_workflow_config to store model_code instead of UUID in model_id column
-- The model_id column should contain the model code (e.g., 'gpt-4-turbo') not the UUID

-- First, update the existing records to use model_code
UPDATE ai_workflow_config awc
SET model_id = (
    SELECT llm.model_code 
    FROM llm_models llm 
    WHERE llm.id::text = awc.model_id
)
WHERE EXISTS (
    SELECT 1 
    FROM llm_models llm 
    WHERE llm.id::text = awc.model_id
);

-- Log what we're updating
DO $$
BEGIN
    RAISE NOTICE 'Updated ai_workflow_config to use model_code instead of UUID';
END $$;