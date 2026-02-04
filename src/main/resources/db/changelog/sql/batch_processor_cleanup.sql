-- Migration pour nettoyer les colonnes inutiles du module batch processor
-- Date: 2025-08-22
-- Description: Suppression des colonnes non essentielles au domaine batch

-- Table batch_book : Supprimer title, author et metadata
ALTER TABLE batch_book DROP COLUMN IF EXISTS title;
ALTER TABLE batch_book DROP COLUMN IF EXISTS author;
ALTER TABLE batch_book DROP COLUMN IF EXISTS metadata;

-- Table batch_model_llm : Supprimer display_name et created_at
ALTER TABLE batch_model_llm DROP COLUMN IF EXISTS display_name;
ALTER TABLE batch_model_llm DROP COLUMN IF EXISTS created_at;

-- Table batch_prompt : Supprimer name, created_at et updated_at
ALTER TABLE batch_prompt DROP COLUMN IF EXISTS name;
ALTER TABLE batch_prompt DROP COLUMN IF EXISTS created_at;
ALTER TABLE batch_prompt DROP COLUMN IF EXISTS updated_at;

-- Commentaires pour documenter les changements
COMMENT ON TABLE batch_book IS 'Table simplifiée pour le traitement batch - données métier supprimées';
COMMENT ON TABLE batch_model_llm IS 'Configuration LLM pour batch - champs non essentiels supprimés';
COMMENT ON TABLE batch_prompt IS 'Prompts pour batch - timestamps et nom supprimés';