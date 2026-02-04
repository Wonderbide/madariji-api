-- Drop the unused default_model column from ai_prompt_template table
-- This column was only used for logging and creates confusion with ai_flow_configuration

ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS default_model;