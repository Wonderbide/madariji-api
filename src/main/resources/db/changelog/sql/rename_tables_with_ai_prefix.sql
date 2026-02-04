-- Rename prompt and model configuration tables with ai_ prefix

-- Rename prompt_template table
ALTER TABLE prompt_template RENAME TO ai_prompt_template;

-- Rename model_configuration table  
ALTER TABLE model_configuration RENAME TO ai_model_configuration;

-- Note: ai_flow_configuration already has the ai_ prefix
-- Note: ai_flow_configuration doesn't have a foreign key to model_configuration
-- It only has model_id as a string field, not a foreign key relationship

-- PostgreSQL automatically updates references in indexes and sequences when tables are renamed