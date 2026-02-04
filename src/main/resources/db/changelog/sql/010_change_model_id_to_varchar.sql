-- Change model_id from UUID to VARCHAR in ai_workflow_config
-- This is needed because model_id now stores model_code which is a string

-- First drop the foreign key constraint if it exists
ALTER TABLE ai_workflow_config 
DROP CONSTRAINT IF EXISTS fk_workflow_model;

-- Change the column type
ALTER TABLE ai_workflow_config 
ALTER COLUMN model_id TYPE VARCHAR(255) USING model_id::text;

-- Update the comment to reflect the change
COMMENT ON COLUMN ai_workflow_config.model_id IS 'Model code reference from llm_models table';