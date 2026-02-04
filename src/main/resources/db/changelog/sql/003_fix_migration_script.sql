-- Fixed migration script - Step by step approach

-- Step 1: Check current state and fix prompt_library
DO $$
BEGIN
    -- Only rename if the table exists with old name
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ai_prompt_template') THEN
        -- Table hasn't been renamed yet, skip for now as it was already renamed
        NULL;
    END IF;
END $$;

-- Step 2: Fix llm_models column names
DO $$
BEGIN
    -- Rename model_id to model_code if needed
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'llm_models' AND column_name = 'model_id') THEN
        ALTER TABLE llm_models RENAME COLUMN model_id TO model_code;
    END IF;
    
    -- Rename default_temperature to temperature if needed
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'llm_models' AND column_name = 'default_temperature') THEN
        ALTER TABLE llm_models RENAME COLUMN default_temperature TO temperature;
    END IF;
    
    -- Rename max_output_tokens to max_tokens if needed
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'llm_models' AND column_name = 'max_output_tokens') THEN
        ALTER TABLE llm_models RENAME COLUMN max_output_tokens TO max_tokens;
    END IF;
END $$;

-- Step 3: Add missing columns to llm_models
ALTER TABLE llm_models
  ADD COLUMN IF NOT EXISTS top_p DECIMAL(3,2) DEFAULT 0.9,
  ADD COLUMN IF NOT EXISTS frequency_penalty DECIMAL(3,2) DEFAULT 0.0,
  ADD COLUMN IF NOT EXISTS presence_penalty DECIMAL(3,2) DEFAULT 0.0,
  ADD COLUMN IF NOT EXISTS context_window INTEGER;

-- Step 4: Fix ai_workflow_config
DO $$
BEGIN
    -- Rename flow_type to workflow_type if needed
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'ai_workflow_config' AND column_name = 'flow_type') THEN
        ALTER TABLE ai_workflow_config RENAME COLUMN flow_type TO workflow_type;
    END IF;
    
    -- Add prompt_id if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ai_workflow_config' AND column_name = 'prompt_id') THEN
        ALTER TABLE ai_workflow_config ADD COLUMN prompt_id UUID;
    END IF;
    
    -- Add model_id if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ai_workflow_config' AND column_name = 'model_id') THEN
        ALTER TABLE ai_workflow_config ADD COLUMN model_id UUID;
    END IF;
    
    -- Add description if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ai_workflow_config' AND column_name = 'description') THEN
        ALTER TABLE ai_workflow_config ADD COLUMN description TEXT;
    END IF;
    
    -- Add configured_by if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ai_workflow_config' AND column_name = 'configured_by') THEN
        ALTER TABLE ai_workflow_config ADD COLUMN configured_by VARCHAR(255);
    END IF;
END $$;

-- Step 5: Update default values in llm_models
UPDATE llm_models SET temperature = 0.05 WHERE temperature IS NULL;
UPDATE llm_models SET max_tokens = 4096 WHERE max_tokens IS NULL;
UPDATE llm_models SET top_p = 0.9 WHERE top_p IS NULL;
UPDATE llm_models SET frequency_penalty = 0.0 WHERE frequency_penalty IS NULL;
UPDATE llm_models SET presence_penalty = 0.0 WHERE presence_penalty IS NULL;

-- Set context windows for known models
UPDATE llm_models SET context_window = 128000 WHERE model_code LIKE 'gpt-4%';
UPDATE llm_models SET context_window = 1000000 WHERE model_code LIKE 'gemini%';
UPDATE llm_models SET context_window = 64000 WHERE model_code LIKE 'deepseek%';

-- Step 6: Link existing workflow configs to prompts and models
UPDATE ai_workflow_config awc
SET 
  prompt_id = (
    SELECT id FROM prompt_library 
    WHERE identifier = CASE
      WHEN awc.workflow_type = 'WORD_ANALYSIS' THEN 'WORD_ANALYSIS_V1'
      WHEN awc.workflow_type = 'PAGE_STRUCTURING' THEN 'PAGE_STRUCTURING_V5'
    END
    LIMIT 1
  ),
  model_id = (
    SELECT id FROM llm_models 
    WHERE model_code = CASE
      WHEN awc.workflow_type = 'WORD_ANALYSIS' THEN 'gpt-4-turbo'
      WHEN awc.workflow_type = 'PAGE_STRUCTURING' THEN 'gemini-2.0-flash-exp'
    END
    LIMIT 1
  ),
  description = COALESCE(description, 'Migrated configuration for ' || awc.workflow_type),
  configured_by = COALESCE(configured_by, 'migration_script')
WHERE prompt_id IS NULL OR model_id IS NULL;

-- Step 7: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_prompt_library_identifier ON prompt_library(identifier);
CREATE INDEX IF NOT EXISTS idx_prompt_library_active ON prompt_library(is_active);
CREATE INDEX IF NOT EXISTS idx_llm_models_code ON llm_models(model_code);
CREATE INDEX IF NOT EXISTS idx_workflow_config_type ON ai_workflow_config(workflow_type);
CREATE INDEX IF NOT EXISTS idx_workflow_config_active ON ai_workflow_config(is_active);