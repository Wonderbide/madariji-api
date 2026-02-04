-- Final cleanup of prompt_library table to match AI_CONFIG_MINIMAL.md specification

-- According to AI_CONFIG_MINIMAL.md, prompt_library should only have:
-- id, prompt_key, prompt_text, is_active, version, created_at, updated_at

-- 1. Drop all unnecessary columns
ALTER TABLE prompt_library 
DROP COLUMN IF EXISTS category,           -- Not in the plan
DROP COLUMN IF EXISTS is_deprecated,      -- Not in the plan
DROP COLUMN IF EXISTS deprecated_at,      -- Not in the plan
DROP COLUMN IF EXISTS deprecated_by,      -- Not in the plan
DROP COLUMN IF EXISTS created_by,         -- Not in the plan
DROP COLUMN IF EXISTS updated_by;         -- Not in the plan

-- 2. Rename columns to match the plan
ALTER TABLE prompt_library 
RENAME COLUMN identifier TO prompt_key;

ALTER TABLE prompt_library 
RENAME COLUMN prompt_content TO prompt_text;

-- 3. Verify final structure matches the plan:
-- Expected columns after this migration:
-- id, prompt_key, prompt_text, is_active, version, created_at, updated_at

DO $$
BEGIN
    RAISE NOTICE 'prompt_library table cleaned up to match AI_CONFIG_MINIMAL.md specification';
END $$;