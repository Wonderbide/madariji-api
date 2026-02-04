-- PHASE 2.1 du IMPLEMENTATION_PLAN_FINAL.md
-- Nettoyer les colonnes inutiles EXACTEMENT comme spécifié dans le plan

-- 1. Nettoyer prompt_library
ALTER TABLE prompt_library 
  DROP COLUMN IF EXISTS category,
  DROP COLUMN IF EXISTS temperature,
  DROP COLUMN IF EXISTS max_tokens;
-- Note: target_language_code déjà supprimée

-- 2. Nettoyer llm_models  
ALTER TABLE llm_models
  DROP COLUMN IF EXISTS is_available,
  DROP COLUMN IF EXISTS performance_tier,
  DROP COLUMN IF EXISTS cost_per_1k_input_tokens,
  DROP COLUMN IF EXISTS cost_per_1k_output_tokens;

-- 3. Nettoyer ai_workflow_config
ALTER TABLE ai_workflow_config
  DROP COLUMN IF EXISTS priority;

-- Colonnes supplémentaires identifiées qui ne sont pas dans le plan mais sont inutiles
-- Je les liste ici mais ne les supprime PAS sans votre accord explicite :
-- llm_models: temperature_override, max_tokens_override, top_p, frequency_penalty, presence_penalty, context_window, avg_response_time_ms, success_rate, supported_features
-- prompt_library: is_deprecated, deprecated_at, deprecated_by, created_by, updated_by  
-- ai_workflow_config: prompt_id, description, configured_by, created_by

DO $$
BEGIN
    RAISE NOTICE 'Suppression des colonnes selon IMPLEMENTATION_PLAN_FINAL.md - Phase 2.1';
END $$;