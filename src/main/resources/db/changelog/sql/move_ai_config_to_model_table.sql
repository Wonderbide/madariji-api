-- Move AI configuration from prompt template to model configuration
-- Step 1: Add prompt-specific override columns to ai_model_configuration if they don't exist

-- Add temperature_override column for prompt-specific temperature
ALTER TABLE ai_model_configuration 
ADD COLUMN IF NOT EXISTS temperature_override DOUBLE PRECISION;

-- Add max_tokens_override column for prompt-specific max tokens  
ALTER TABLE ai_model_configuration
ADD COLUMN IF NOT EXISTS max_tokens_override INTEGER;

-- Step 2: Update existing model configurations with default values from prompt templates
-- We'll use the most common values as defaults where default_temperature is null
UPDATE ai_model_configuration
SET default_temperature = 0.05
WHERE default_temperature IS NULL 
AND model_id IN ('gpt-4-turbo', 'gpt-4-turbo-preview', 'gpt-4o', 'gpt-4o-mini');

UPDATE ai_model_configuration
SET default_temperature = 0.05
WHERE default_temperature IS NULL
AND model_id LIKE 'gemini%';

UPDATE ai_model_configuration  
SET default_temperature = 0.05
WHERE default_temperature IS NULL
AND model_id LIKE 'deepseek%';

-- Set max_output_tokens defaults where missing
UPDATE ai_model_configuration
SET max_output_tokens = 4096
WHERE max_output_tokens IS NULL
AND model_id IN ('gpt-4-turbo', 'gpt-4-turbo-preview', 'gpt-4o', 'gpt-4o-mini');

UPDATE ai_model_configuration
SET max_output_tokens = 8192  
WHERE max_output_tokens IS NULL
AND model_id LIKE 'gemini%';

UPDATE ai_model_configuration
SET max_output_tokens = 4096
WHERE max_output_tokens IS NULL  
AND model_id LIKE 'deepseek%';

-- Step 3: Drop the columns from ai_prompt_template
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS temperature;
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS max_tokens;