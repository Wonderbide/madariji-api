-- Fix mismatched models and providers in ai_workflow_config
-- Ensure OpenAI provider uses OpenAI models and Google provider uses Gemini models

-- Fix OpenAI workflows to use GPT models
UPDATE ai_workflow_config 
SET model_id = 'gpt-4-turbo'
WHERE provider = 'OPENAI' 
AND model_id LIKE 'gemini%';

-- Fix Google workflows to use Gemini models  
UPDATE ai_workflow_config
SET model_id = 'gemini-2.0-flash-exp'
WHERE provider = 'GOOGLE'
AND model_id LIKE 'gpt%';

-- Fix DeepSeek workflows to use DeepSeek models
UPDATE ai_workflow_config
SET model_id = 'deepseek-chat'
WHERE provider = 'DEEPSEEK'
AND model_id NOT LIKE 'deepseek%';

-- Log the fixes
DO $$
BEGIN
    RAISE NOTICE 'Fixed provider-model mismatches in ai_workflow_config';
END $$;