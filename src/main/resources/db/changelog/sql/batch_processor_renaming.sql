-- Migration pour renommer les colonnes et rendre le module batch processor agnostique
-- Date: 2025-08-23
-- Description: Renommage des colonnes pour supprimer les références aux providers spécifiques

-- 1. Ajouter la colonne provider_type pour identifier le provider LLM
-- IMPORTANT: Pour l'instant on met 'OPENAI' par défaut car c'est le seul provider actif
-- TODO: À terme, cette valeur devra être définie dynamiquement depuis batch_model_llm.provider
ALTER TABLE batch_book ADD COLUMN IF NOT EXISTS provider_type VARCHAR(50) DEFAULT 'OPENAI';

-- Mettre à jour les enregistrements existants pour avoir explicitement OPENAI
UPDATE batch_book SET provider_type = 'OPENAI' WHERE provider_type IS NULL;

-- 2. Renommer les colonnes OpenAI en colonnes génériques
ALTER TABLE batch_book RENAME COLUMN openai_batch_id TO provider_batch_id;
ALTER TABLE batch_book RENAME COLUMN openai_file_id TO provider_file_ref;

-- 3. Renommer les colonnes Vision (Google Vision API) en OCR (générique)
ALTER TABLE batch_book RENAME COLUMN vision_path TO ocr_output_path;
ALTER TABLE batch_book RENAME COLUMN vision_completed_at TO ocr_completed_at;
ALTER TABLE batch_book RENAME COLUMN vision_operation_name TO ocr_operation_id;

-- 4. Ajouter des commentaires pour documenter les changements
COMMENT ON COLUMN batch_book.provider_type IS 'Type de provider LLM utilisé (OPENAI, GEMINI, etc.)';
COMMENT ON COLUMN batch_book.provider_batch_id IS 'ID du batch chez le provider LLM (ex: batch_abc123 pour OpenAI, batches/123456 pour Gemini)';
COMMENT ON COLUMN batch_book.provider_file_ref IS 'Référence du fichier chez le provider (file_id pour OpenAI, uri pour Gemini)';
COMMENT ON COLUMN batch_book.ocr_output_path IS 'Chemin de sortie des résultats OCR (agnostique du provider OCR)';
COMMENT ON COLUMN batch_book.ocr_completed_at IS 'Timestamp de fin du traitement OCR';
COMMENT ON COLUMN batch_book.ocr_operation_id IS 'ID de l''opération OCR (agnostique du provider)';