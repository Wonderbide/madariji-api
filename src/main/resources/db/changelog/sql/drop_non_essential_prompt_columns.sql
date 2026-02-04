-- Drop non-essential columns from ai_prompt_template table
-- These columns are not critical for system functionality

-- Drop name column (only used in admin UI)
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS name;

-- Drop description column (only used for documentation)
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS description;

-- Drop input_schema column (feature never implemented)
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS input_schema;

-- Drop output_schema column (feature never implemented)
ALTER TABLE ai_prompt_template DROP COLUMN IF EXISTS output_schema;