-- Update Word Analysis prompt to use dynamic language parameter instead of hardcoded language conditions
-- This migration updates the WORD_ANALYSIS_V1 prompt to be more flexible

-- First, deactivate the old version
UPDATE prompt_template
SET is_active = false
WHERE identifier = 'WORD_ANALYSIS_V1' 
AND is_active = true;

-- Insert the new version with dynamic language support
INSERT INTO prompt_template (
    id,
    identifier,
    name,
    description,
    category,
    version,
    is_active,
    is_deprecated,
    default_model,
    temperature,
    max_tokens,
    prompt_content,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'WORD_ANALYSIS_V1',
    'Word Analysis - Dynamic Multilingual',
    'Analyzes Arabic words with grammatical and semantic information in any dynamically specified target language',
    'WORD_ANALYSIS',
    '3.0',
    true,
    false,
    'gpt-4-turbo',
    0.4,
    2048,
    '**Tâche :** Analyse grammaticalement et sémantiquement le mot arabe cible fourni, en utilisant **impérativement le contexte de paragraphe fourni** pour déterminer le sens et la traduction les plus précis dans la langue demandée (%s). Ensuite, identifie et fournis sa forme canonique (forme de dictionnaire) selon les règles spécifiques.

**Contexte Fourni :**
*   **Mot Cible :** %s
*   **Titre du Livre :** %s
*   **Contexte de Paragraphe (extrait de la page actuelle) :**
--- DEBUT CONTEXTE ---
%s
--- FIN CONTEXTE ---
*   **Langue de traduction demandée :** %s

**Instructions Générales :**
1.  Concentre ton analyse **exclusivement sur le "Mot Cible"** fourni ci-dessus (`%s`).
2.  **CRITIQUE** : Lis attentivement le "Contexte de Paragraphe" pour comprendre si le mot fait partie d''une expression idiomatique. Cela influence UNIQUEMENT le champ "meaning", PAS le champ "translation".
3.  Détermine le type grammatical principal du "Mot Cible" ("اسم", "فعل", "حرف", ou "unknown").
    **RÈGLE IMPORTANTE POUR LES HARFS**: Si le mot est annexé à un harf (comme بـ, لـ, كـ, فـ, وـ), le mot principal doit être considéré comme un "اسم" (ism) et non comme un "حرف" (harf).
4.  Identifie la forme lemmatisée/canonique du "Mot Cible" (pas la racine de 3 lettres, mais la forme de dictionnaire complète avec ses lettres essentielles).
5.  **TRADUCTION DYNAMIQUE** : Fournis la traduction du "Mot Cible" dans la langue spécifiée (%s). TOUJOURS donner la traduction du mot seul, comme dans un dictionnaire. IGNORE complètement toute expression idiomatique pour ce champ.
6.  Remplis l''objet `details` selon le type grammatical du "Mot Cible" :
    *   Si "اسم" : clés `"meaning"` (sens contextuel précis dans ce paragraphe - métaphorique si applicable !, expliqué dans la langue %s), `"singular"`, `"dual"`, `"plural"`.
    *   Si "فعل" : clés `"meaning"` (sens contextuel précis dans ce paragraphe - métaphorique si applicable !, expliqué dans la langue %s), `"madi"`, `"mudari"`, `"amr"`, `"wazn"`.
    *   Si "حرف" : clés `"meaning"` (sens contextuel précis dans ce paragraphe - métaphorique si applicable !, expliqué dans la langue %s), `"function"` (rôle grammatical).
    *   Si "unknown" : `details` est `null`.
    **DISTINCTION ABSOLUE entre "translation" et "meaning"** :
    - Le champ "translation" : UNIQUEMENT la traduction du mot isolé dans la langue demandée, comme dans un dictionnaire
    - Le champ "meaning" : Le sens du mot dans ce contexte précis, incluant les expressions idiomatiques, expliqué dans la langue demandée
    - Règle SIMPLE : Traduis le mot comme s''il était seul dans un dictionnaire pour "translation"
7.  **Identifie les champs lexicaux** : Fournis de 1 à 3 champs lexicaux (thématiques) auxquels appartient le mot dans un tableau `"lexical_fields"`. Exemples : "cuisine", "travail", "éducation", "santé", "nature", "émotions", "religion", "voyage", "famille", "temps", "lieu", "couleurs", "nourriture", etc.
8.  **NOUVEAU ET CRITIQUE :** Identifie et fournis la **forme canonique (forme de dictionnaire)** du "Mot Cible" dans un champ JSON séparé appelé `"canonical_form"`. Applique les règles suivantes **strictement** :
    *   **Verbe :** La forme passée (ماضي), 3ème personne du singulier masculin (هو). **Inclure la vocalisation (tashkeel)** pour distinguer les formes (وزن).
    *   **Nom (pluriel régulier masculin/féminin, duel) :** La forme singulière vocalisée au cas nominatif (مرفوع).
    *   **Nom Singulier :** La forme singulière vocalisée au cas nominatif (مرفوع).
    *   **Nom Pluriel Irrégulier (جمع تكسير) :** La forme plurielle vocalisée au cas nominatif (مرفوع).
    *   **Autre (Harf, etc.) :** La forme de base vocalisée.
    *   **CRUCIAL pour les NOMS :** Toujours ramener au cas NOMINATIF (مرفوع) :
        - Si le mot est منصوب (accusatif) avec فتحة ou ـًا, le ramener à la forme مرفوع avec ضمة ou ـٌ
        - Si le mot est مجرور (génitif) avec كسرة ou ـٍ, le ramener à la forme مرفوع
        - Si le mot est مجزوم, le ramener à sa forme complète
    *   **Important :** Cette forme canonique doit être nettoyée des articles définis (''ال'') et des prépositions/pronoms communs attachés, **SAUF** si ces lettres font partie de la racine ou de la structure essentielle du mot.
    *   **TRÈS IMPORTANT :** Le champ "root" doit contenir la forme lemmatisée complète, PAS juste la racine de trois lettres.

**FORMAT DE SORTIE JSON STRICT ET RÈGLE CRITIQUE :**
1.  Ta réponse doit être **UNIQUEMENT** un objet JSON valide. Pas de texte avant/après. Pas de ```json.
2.  Structure JSON attendue :
    ```json
    {
      "word": "VALEUR_EXACTE_DU_MOT_CIBLE_FOURNI",
      "type": "TYPE_TROUVÉ",
      "translation": "TRADUCTION_DANS_LA_LANGUE_DEMANDÉE",
      "root": "FORME_LEMMATISÉE_COMPLÈTE" | null,
      "details": { /* OBJET DÉTAILS SELON TYPE */ } | null,
      "canonical_form": "FORME_CANONIQUE_SELON_RÈGLES",
      "lexical_fields": ["champ1", "champ2", "champ3"]
    }
    ```
3.  **RÈGLE ABSOLUMENT CRITIQUE : La valeur de la clé `"word"` dans ta réponse JSON DOIT ÊTRE EXACTEMENT LA MÊME que la chaîne de caractères fournie comme "Mot Cible" au début de ce prompt (`%s`). Ne la modifie PAS et ne la remplace par aucun autre mot, même s''il est présent dans le contexte.**

**INSTRUCTION FINALE SIMPLE** :
- "translation" = traduction directe du mot seul (dictionnaire) dans la langue demandée
- "meaning" = explication du mot dans ce contexte (inclut expressions idiomatiques) dans la langue demandée
- Le contexte influence SEULEMENT "meaning", jamais "translation"

**RAPPEL** : "translation" = dictionnaire du mot seul, "meaning" = contexte et expressions

**Exécute maintenant l''analyse pour le "Mot Cible" : %s**',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (identifier, version) DO NOTHING;

-- Note: Check via application logs if the insert was successful