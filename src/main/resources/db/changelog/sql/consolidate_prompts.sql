-- Migration de consolidation du systeme de prompts
-- Date: 2026-01-28
-- Objectif: S'assurer que les prompts requis existent et supprimer les tables batch obsoletes

-- 1. S'assurer que WORD_ANALYSIS_V1 existe (version 3.0)
INSERT INTO prompt_library (id, prompt_key, prompt_text, version, is_active, created_at, updated_at)
SELECT gen_random_uuid(),
       'WORD_ANALYSIS_V1',
       '**Tache :** Analyse grammaticalement et semantiquement le mot arabe cible fourni...',
       '3.0',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM prompt_library WHERE prompt_key = 'WORD_ANALYSIS_V1' AND is_active = true
);

-- 2. S'assurer que PAGE_STRUCTURING_V5 existe
INSERT INTO prompt_library (id, prompt_key, prompt_text, version, is_active, created_at, updated_at)
SELECT gen_random_uuid(),
       'PAGE_STRUCTURING_V5',
       'Advanced document structuring: Analyze the OCR text and create a precise page-by-page structure.
    Input: %s

    Requirements:
    - Identify exact page boundaries
    - Preserve all content including headers/footers
    - Maintain paragraph structure
    - Detect page numbers

    Output: JSON with pages array containing page_number, content, paragraphs.',
       '5.0',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM prompt_library WHERE prompt_key = 'PAGE_STRUCTURING_V5' AND is_active = true
);

-- 3. Supprimer les tables batch obsoletes (si elles existent)
DROP TABLE IF EXISTS batch_prompt CASCADE;
DROP TABLE IF EXISTS batch_model_llm CASCADE;
