-- Insertion des données initiales pour les tables batch_prompt et batch_model_llm

-- Insertion du prompt actif (anciennement PAGE_STRUCTURING_V5)
INSERT INTO batch_prompt (prompt_text, is_active)
VALUES (
    '# ANALYSE DE PAGE ARABE - OPENAI GPT-4

## CONTEXTE
Analyse et restructure le texte arabe OCR d''une page de livre académique ou religieux.

## OBJECTIFS PRIORITAIRES
1. 🎯 **Vocalisation complète** (Tashkeel) - OBLIGATOIRE pour tout texte arabe
2. 📐 **Reconstruction logique** des blocs fragmentés par l''OCR
3. 🔗 **Gestion intelligente** des références et notes de bas de page
4. ✨ **Amélioration de la lisibilité** (espacement correct de "و")
5. 🧩 **Préservation totale** du contenu original - AUCUNE perte

## FORMAT DE SORTIE STRICT (JSON UNIQUEMENT)
{
  "keep_page": true | false,
  "blocks": [
    {
      "block_type": "<TYPE>",
      "block_text": "<TEXTE_ARABE_VOCALISÉ_ET_CORRIGÉ>"
    }
  ]
}

## TYPES DE BLOCS AUTORISÉS
- heading1 : Titres principaux de chapitre/section
- heading2 : Sous-titres et titres secondaires
- paragraph : Paragraphes de contenu principal
- list_item : Éléments de liste ou énumération
- header : En-têtes de page (nom livre, auteur)
- footer : Pieds de page (informations éditoriales)
- page_number : Numérotation de page
- footnote : Notes de bas de page et commentaires

## RÈGLES DE TRAITEMENT

### 🎯 VOCALISATION (Tashkeel)
- OBLIGATOIRE : Applique Tashkeel complet à tout texte arabe
- Précision : Utilise tes connaissances grammaticales pour une vocalisation correcte

### 📐 RECONSTRUCTION OCR
- Réassemble les mots/phrases fragmentés par l''OCR
- Corrige l''ordre logique des éléments mal positionnés
- Préserve 100% du contenu original - aucune omission

### 🔗 GESTION DES NOTES
- Identifie les appels de notes (¹²³*†) dans le texte principal
- Remplace par des exposants Unicode appropriés
- Positionne correctement après le mot concerné

### ✨ ESPACEMENT "و"
- Format : espace + و + espace pour une lisibilité optimale

### 📋 CRITÈRES DE REJET (keep_page: false)
- Pages complètement vides ou illisibles
- Pages de couverture sans contenu textuel significatif
- Pages publicitaires ou promotionnelles sans rapport avec le livre
- Pages de copyright uniquement (sans autre contenu)

### ✅ CRITÈRES D''ACCEPTATION (keep_page: true)
- Toute page avec du contenu textuel arabe lisible
- Pages d''introduction, préface, ou présentation
- Pages de table des matières ou index
- Pages avec au moins un paragraphe de texte cohérent
- Pages de titre avec informations sur l''auteur ou l''œuvre

IMPORTANT: Réponds UNIQUEMENT avec le JSON, sans aucun texte avant ou après.

**Texte OCR à analyser:**
{{ocr_text}}
',
    true
);

-- Insertion de la configuration du modèle GPT-4o
INSERT INTO batch_model_llm (provider, model_code, max_tokens, temperature, is_active, config_json)
VALUES (
    'OPENAI',
    'gpt-4o',
    4096,
    0.3,
    true,
    '{"response_format": {"type": "json_object"}, "timeout": 60}'::jsonb
);

-- Optionnel : Ajouter d'autres modèles inactifs pour référence future
INSERT INTO batch_model_llm (provider, model_code, max_tokens, temperature, is_active, config_json)
VALUES 
(
    'OPENAI',
    'gpt-4o-mini',
    4096,
    0.3,
    false,
    '{"response_format": {"type": "json_object"}, "timeout": 30}'::jsonb
),
(
    'GOOGLE',
    'gemini-2.0-flash-exp',
    8192,
    0.3,
    false,
    '{"response_format": {"type": "json_object"}, "timeout": 30}'::jsonb
);