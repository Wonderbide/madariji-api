-- PHASE 1: Rename tables to new architecture names
ALTER TABLE ai_prompt_template RENAME TO prompt_library;
ALTER TABLE ai_model_configuration RENAME TO llm_models;
ALTER TABLE ai_flow_configuration RENAME TO ai_workflow_config;

-- PHASE 2: Clean up prompt_library columns
ALTER TABLE prompt_library 
  DROP COLUMN IF EXISTS temperature,
  DROP COLUMN IF EXISTS max_tokens,
  DROP COLUMN IF EXISTS name,
  DROP COLUMN IF EXISTS description,
  DROP COLUMN IF EXISTS input_schema,
  DROP COLUMN IF EXISTS output_schema,
  DROP COLUMN IF EXISTS default_model,
  ALTER COLUMN identifier TYPE VARCHAR(100),
  ALTER COLUMN prompt_content SET NOT NULL;

-- Remove target_language_code as it will be passed dynamically
ALTER TABLE prompt_library
  DROP COLUMN IF EXISTS target_language_code;

-- PHASE 3: Clean up llm_models columns
ALTER TABLE llm_models
  DROP COLUMN IF EXISTS is_available,
  DROP COLUMN IF EXISTS performance_tier,
  DROP COLUMN IF EXISTS temperature_override,
  DROP COLUMN IF EXISTS max_tokens_override;

-- Rename columns (must be separate statements in PostgreSQL)
ALTER TABLE llm_models RENAME COLUMN model_id TO model_code;
ALTER TABLE llm_models RENAME COLUMN default_temperature TO temperature;
ALTER TABLE llm_models RENAME COLUMN max_output_tokens TO max_tokens;

-- Add missing configuration columns if they don't exist
ALTER TABLE llm_models
  ADD COLUMN IF NOT EXISTS top_p DECIMAL(3,2) DEFAULT 0.9,
  ADD COLUMN IF NOT EXISTS frequency_penalty DECIMAL(3,2) DEFAULT 0.0,
  ADD COLUMN IF NOT EXISTS presence_penalty DECIMAL(3,2) DEFAULT 0.0,
  ADD COLUMN IF NOT EXISTS context_window INTEGER,
  ALTER COLUMN temperature SET DEFAULT 0.05;

-- PHASE 4: Transform ai_workflow_config
-- Rename column first (must be separate statement)
ALTER TABLE ai_workflow_config RENAME COLUMN flow_type TO workflow_type;

ALTER TABLE ai_workflow_config
  DROP COLUMN IF EXISTS provider,
  DROP COLUMN IF EXISTS model_id,
  DROP COLUMN IF EXISTS priority,
  ADD COLUMN IF NOT EXISTS prompt_id UUID,
  ADD COLUMN IF NOT EXISTS model_id UUID,
  ADD COLUMN IF NOT EXISTS description TEXT,
  ADD COLUMN IF NOT EXISTS configured_by VARCHAR(255);

-- PHASE 5: Add foreign key constraints
ALTER TABLE ai_workflow_config
  ADD CONSTRAINT fk_workflow_prompt 
    FOREIGN KEY (prompt_id) REFERENCES prompt_library(id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_workflow_model 
    FOREIGN KEY (model_id) REFERENCES llm_models(id) ON DELETE CASCADE;

-- PHASE 6: Create unique constraint to prevent duplicate active configs
CREATE UNIQUE INDEX idx_unique_active_workflow 
  ON ai_workflow_config(workflow_type) 
  WHERE is_active = true;

-- PHASE 7: Update model configurations with proper defaults
UPDATE llm_models SET temperature = 0.05 WHERE temperature IS NULL;
UPDATE llm_models SET max_tokens = 4096 WHERE max_tokens IS NULL;
UPDATE llm_models SET top_p = 0.9 WHERE top_p IS NULL;
UPDATE llm_models SET frequency_penalty = 0.0 WHERE frequency_penalty IS NULL;
UPDATE llm_models SET presence_penalty = 0.0 WHERE presence_penalty IS NULL;

-- Set context windows for known models
UPDATE llm_models SET context_window = 128000 WHERE model_code LIKE 'gpt-4%';
UPDATE llm_models SET context_window = 1000000 WHERE model_code LIKE 'gemini%';
UPDATE llm_models SET context_window = 64000 WHERE model_code LIKE 'deepseek%';

-- PHASE 8: Migrate existing flow configurations to new structure
-- First, create temporary mapping
WITH prompt_mapping AS (
  SELECT 
    CASE 
      WHEN workflow_type = 'WORD_ANALYSIS' THEN 'WORD_ANALYSIS_V1'
      WHEN workflow_type = 'PAGE_STRUCTURING' THEN 'PAGE_STRUCTURING_V5'
    END as prompt_identifier,
    workflow_type,
    awc.id as workflow_id
  FROM ai_workflow_config awc
),
model_mapping AS (
  SELECT DISTINCT ON (model_code)
    id as model_id,
    model_code
  FROM llm_models
  WHERE model_code IN (
    'gpt-4-turbo', 'gpt-4o', 'gpt-4o-mini',
    'gemini-2.0-flash-exp', 'gemini-pro',
    'deepseek-v2', 'deepseek-chat'
  )
)
UPDATE ai_workflow_config awc
SET 
  prompt_id = pl.id,
  model_id = (
    SELECT lm.id 
    FROM llm_models lm 
    WHERE lm.model_code = CASE
      WHEN awc.workflow_type = 'WORD_ANALYSIS' THEN 'gpt-4-turbo'
      WHEN awc.workflow_type = 'PAGE_STRUCTURING' THEN 'gemini-2.0-flash-exp'
    END
    LIMIT 1
  ),
  description = 'Migrated configuration for ' || awc.workflow_type,
  configured_by = 'migration_script'
FROM prompt_mapping pm
JOIN prompt_library pl ON pl.identifier = pm.prompt_identifier
WHERE awc.id = pm.workflow_id;

-- PHASE 9: Create missing workflow configurations if needed
INSERT INTO ai_workflow_config (
  id,
  workflow_type,
  prompt_id,
  model_id,
  is_active,
  description,
  created_at,
  updated_at,
  configured_by
)
SELECT 
  gen_random_uuid(),
  'WORD_ANALYSIS',
  pl.id,
  lm.id,
  true,
  'Word analysis using GPT-4 Turbo',
  NOW(),
  NOW(),
  'migration_script'
FROM prompt_library pl
CROSS JOIN llm_models lm
WHERE pl.identifier = 'WORD_ANALYSIS_V1'
  AND pl.is_active = true
  AND lm.model_code = 'gpt-4-turbo'
  AND NOT EXISTS (
    SELECT 1 FROM ai_workflow_config WHERE workflow_type = 'WORD_ANALYSIS'
  );

INSERT INTO ai_workflow_config (
  id,
  workflow_type,
  prompt_id,
  model_id,
  is_active,
  description,
  created_at,
  updated_at,
  configured_by
)
SELECT 
  gen_random_uuid(),
  'PAGE_STRUCTURING',
  pl.id,
  lm.id,
  true,
  'Page structuring using Gemini Flash',
  NOW(),
  NOW(),
  'migration_script'
FROM prompt_library pl
CROSS JOIN llm_models lm
WHERE pl.identifier = 'PAGE_STRUCTURING_V5'
  AND pl.is_active = true
  AND lm.model_code = 'gemini-2.0-flash-exp'
  AND NOT EXISTS (
    SELECT 1 FROM ai_workflow_config WHERE workflow_type = 'PAGE_STRUCTURING'
  );

-- PHASE 10: Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_prompt_library_identifier ON prompt_library(identifier);
CREATE INDEX IF NOT EXISTS idx_prompt_library_active ON prompt_library(is_active);
CREATE INDEX IF NOT EXISTS idx_llm_models_code ON llm_models(model_code);
CREATE INDEX IF NOT EXISTS idx_workflow_config_type ON ai_workflow_config(workflow_type);
CREATE INDEX IF NOT EXISTS idx_workflow_config_active ON ai_workflow_config(is_active);