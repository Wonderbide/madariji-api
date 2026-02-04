-- Création des tables indépendantes pour le batch processor
-- Ces tables remplacent la dépendance vers prompt_library et ai_workflow_config

-- Table pour stocker les prompts OCR (un seul actif à la fois)
CREATE TABLE IF NOT EXISTS batch_prompt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    prompt_text TEXT NOT NULL,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table pour stocker les configurations de modèles LLM (un seul actif à la fois)
CREATE TABLE IF NOT EXISTS batch_model_llm (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(50) NOT NULL, -- OPENAI, GOOGLE
    model_code VARCHAR(100) NOT NULL, -- gpt-4o-mini, gpt-4o, gemini-2.0-flash
    display_name VARCHAR(100) NOT NULL,
    max_tokens INTEGER DEFAULT 4096,
    temperature DECIMAL(3,2) DEFAULT 0.3,
    is_active BOOLEAN DEFAULT false,
    config_json JSONB, -- Configurations supplémentaires si nécessaire
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Contraintes pour garantir un seul actif (utilise CREATE UNIQUE INDEX au lieu de CONSTRAINT)
CREATE UNIQUE INDEX unique_active_prompt ON batch_prompt(is_active) WHERE is_active = true;
CREATE UNIQUE INDEX unique_active_model ON batch_model_llm(is_active) WHERE is_active = true;

-- Index additionnels pour performance
CREATE INDEX idx_batch_prompt_name ON batch_prompt(name);
CREATE INDEX idx_batch_model_provider ON batch_model_llm(provider, model_code);

-- Commentaires
COMMENT ON TABLE batch_prompt IS 'Prompts pour l''analyse OCR dans le batch processor';
COMMENT ON TABLE batch_model_llm IS 'Configurations des modèles LLM pour le batch processor';
COMMENT ON COLUMN batch_prompt.is_active IS 'Un seul prompt peut être actif à la fois';
COMMENT ON COLUMN batch_model_llm.is_active IS 'Un seul modèle peut être actif à la fois';