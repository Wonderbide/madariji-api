# Archive des Prompts - Reference Historique

> Ce fichier contient toutes les versions des prompts pour reference future.
> Seuls WORD_ANALYSIS_V1 et PAGE_STRUCTURING_V5 sont actifs en production.

---

## WORD_ANALYSIS_V1 (v1.0) - Version initiale

```
You are a linguistics expert. Analyze the word "%s" in the context: "%s".
Provide a detailed analysis including:
1. Translation to %s
2. Root word and etymology
3. Grammatical category
4. Usage examples
5. Related words
Return the response in JSON format.
```

---

## WORD_ANALYSIS_V1 (v3.0) - ACTIF - Version multilingue dynamique

```
**Tache :** Analyse grammaticalement et semantiquement le mot arabe cible fourni, en utilisant **imperativement le contexte de paragraphe fourni** pour determiner le sens et la traduction les plus precis dans la langue demandee (%s). Ensuite, identifie et fournis sa forme canonique (forme de dictionnaire) selon les regles specifiques.

**Contexte Fourni :**
*   **Mot Cible :** %s
*   **Titre du Livre :** %s
*   **Contexte de Paragraphe (extrait de la page actuelle) :**
--- DEBUT CONTEXTE ---
%s
--- FIN CONTEXTE ---
*   **Langue de traduction demandee :** %s

**Instructions Generales :**
1.  Concentre ton analyse **exclusivement sur le "Mot Cible"** fourni ci-dessus (`%s`).
2.  **CRITIQUE** : Lis attentivement le "Contexte de Paragraphe" pour comprendre si le mot fait partie d'une expression idiomatique. Cela influence UNIQUEMENT le champ "meaning", PAS le champ "translation".
3.  Determine le type grammatical principal du "Mot Cible" ("اسم", "فعل", "حرف", ou "unknown").
    **REGLE IMPORTANTE POUR LES HARFS**: Si le mot est annexe a un harf (comme بـ, لـ, كـ, فـ, وـ), le mot principal doit etre considere comme un "اسم" (ism) et non comme un "حرف" (harf).
4.  Identifie la forme lemmatisee/canonique du "Mot Cible" (pas la racine de 3 lettres, mais la forme de dictionnaire complete avec ses lettres essentielles).
5.  **TRADUCTION DYNAMIQUE** : Fournis la traduction du "Mot Cible" dans la langue specifiee (%s). TOUJOURS donner la traduction du mot seul, comme dans un dictionnaire. IGNORE completement toute expression idiomatique pour ce champ.
6.  Remplis l'objet `details` selon le type grammatical du "Mot Cible" :
    *   Si "اسم" : cles `"meaning"` (sens contextuel precis dans ce paragraphe - metaphorique si applicable !, explique dans la langue %s), `"singular"`, `"dual"`, `"plural"`.
    *   Si "فعل" : cles `"meaning"` (sens contextuel precis dans ce paragraphe - metaphorique si applicable !, explique dans la langue %s), `"madi"`, `"mudari"`, `"amr"`, `"wazn"`.
    *   Si "حرف" : cles `"meaning"` (sens contextuel precis dans ce paragraphe - metaphorique si applicable !, explique dans la langue %s), `"function"` (role grammatical).
    *   Si "unknown" : `details` est `null`.
    **DISTINCTION ABSOLUE entre "translation" et "meaning"** :
    - Le champ "translation" : UNIQUEMENT la traduction du mot isole dans la langue demandee, comme dans un dictionnaire
    - Le champ "meaning" : Le sens du mot dans ce contexte precis, incluant les expressions idiomatiques, explique dans la langue demandee
    - Regle SIMPLE : Traduis le mot comme s'il etait seul dans un dictionnaire pour "translation"
7.  **Identifie les champs lexicaux** : Fournis de 1 a 3 champs lexicaux (thematiques) auxquels appartient le mot dans un tableau `"lexical_fields"`. Exemples : "cuisine", "travail", "education", "sante", "nature", "emotions", "religion", "voyage", "famille", "temps", "lieu", "couleurs", "nourriture", etc.
8.  **NOUVEAU ET CRITIQUE :** Identifie et fournis la **forme canonique (forme de dictionnaire)** du "Mot Cible" dans un champ JSON separe appele `"canonical_form"`. Applique les regles suivantes **strictement** :
    *   **Verbe :** La forme passee (ماضي), 3eme personne du singulier masculin (هو). **Inclure la vocalisation (tashkeel)** pour distinguer les formes (وزن).
    *   **Nom (pluriel regulier masculin/feminin, duel) :** La forme singuliere vocalisee au cas nominatif (مرفوع).
    *   **Nom Singulier :** La forme singuliere vocalisee au cas nominatif (مرفوع).
    *   **Nom Pluriel Irregulier (جمع تكسير) :** La forme plurielle vocalisee au cas nominatif (مرفوع).
    *   **Autre (Harf, etc.) :** La forme de base vocalisee.
    *   **CRUCIAL pour les NOMS :** Toujours ramener au cas NOMINATIF (مرفوع) :
        - Si le mot est منصوب (accusatif) avec فتحة ou ـًا, le ramener a la forme مرفوع avec ضمة ou ـٌ
        - Si le mot est مجرور (genitif) avec كسرة ou ـٍ, le ramener a la forme مرفوع
        - Si le mot est مجزوم, le ramener a sa forme complete
    *   **Important :** Cette forme canonique doit etre nettoyee des articles definis ('ال') et des prepositions/pronoms communs attaches, **SAUF** si ces lettres font partie de la racine ou de la structure essentielle du mot.
    *   **TRES IMPORTANT :** Le champ "root" doit contenir la forme lemmatisee complete, PAS juste la racine de trois lettres.

**FORMAT DE SORTIE JSON STRICT ET REGLE CRITIQUE :**
1.  Ta reponse doit etre **UNIQUEMENT** un objet JSON valide. Pas de texte avant/apres. Pas de ```json.
2.  Structure JSON attendue :
    {
      "word": "VALEUR_EXACTE_DU_MOT_CIBLE_FOURNI",
      "type": "TYPE_TROUVE",
      "translation": "TRADUCTION_DANS_LA_LANGUE_DEMANDEE",
      "root": "FORME_LEMMATISEE_COMPLETE" | null,
      "details": { /* OBJET DETAILS SELON TYPE */ } | null,
      "canonical_form": "FORME_CANONIQUE_SELON_REGLES",
      "lexical_fields": ["champ1", "champ2", "champ3"]
    }
3.  **REGLE ABSOLUMENT CRITIQUE : La valeur de la cle `"word"` dans ta reponse JSON DOIT ETRE EXACTEMENT LA MEME que la chaine de caracteres fournie comme "Mot Cible" au debut de ce prompt (`%s`). Ne la modifie PAS et ne la remplace par aucun autre mot, meme s'il est present dans le contexte.**

**INSTRUCTION FINALE SIMPLE** :
- "translation" = traduction directe du mot seul (dictionnaire) dans la langue demandee
- "meaning" = explication du mot dans ce contexte (inclut expressions idiomatiques) dans la langue demandee
- Le contexte influence SEULEMENT "meaning", jamais "translation"

**RAPPEL** : "translation" = dictionnaire du mot seul, "meaning" = contexte et expressions

**Execute maintenant l'analyse pour le "Mot Cible" : %s**
```

---

## PAGE_STRUCTURING_V1 (v1.0) - Inactif

```
You are a document structure analyzer. Analyze the following OCR text and structure it into pages.
Text: %s

Identify:
1. Page breaks
2. Headers and footers
3. Paragraphs
4. Main content vs metadata

Return as structured JSON with page numbers and content.
```

---

## PAGE_STRUCTURING_V2 (v2.0) - Inactif

```
Analyze and structure this OCR text into pages. Text: %s
Return JSON with page_number, content, and metadata.
```

---

## PAGE_STRUCTURING_V3 (v3.0) - Inactif

```
Structure OCR text: %s. Identify pages, paragraphs, headers. Return structured JSON.
```

---

## PAGE_STRUCTURING_V4 (v4.0) - Inactif

```
Parse and structure document: %s. Extract pages with clear boundaries. Format as JSON.
```

---

## PAGE_STRUCTURING_V5 (v5.0) - ACTIF

```
Advanced document structuring: Analyze the OCR text and create a precise page-by-page structure.
Input: %s

Requirements:
- Identify exact page boundaries
- Preserve all content including headers/footers
- Maintain paragraph structure
- Detect page numbers

Output: JSON with pages array containing page_number, content, paragraphs.
```

---

## BATCH_PROMPT_OPENAI (Supprime - etait dans batch_prompt)

> Ce prompt etait utilise pour le traitement batch avec OpenAI GPT-4.
> Il a ete supprime car le systeme batch_prompt etait une duplication inutile.

```
# ANALYSE DE PAGE ARABE - OPENAI GPT-4

## CONTEXTE
Analyse et restructure le texte arabe OCR d'une page de livre academique ou religieux.

## OBJECTIFS PRIORITAIRES
1. VOCALISATION COMPLETE (Tashkeel) - OBLIGATOIRE pour tout texte arabe
2. RECONSTRUCTION LOGIQUE des blocs fragmentes par l'OCR
3. GESTION INTELLIGENTE des references et notes de bas de page
4. AMELIORATION DE LA LISIBILITE (espacement correct de "و")
5. PRESERVATION TOTALE du contenu original - AUCUNE perte

## FORMAT DE SORTIE STRICT (JSON UNIQUEMENT)
{
  "keep_page": true | false,
  "blocks": [
    {
      "block_type": "<TYPE>",
      "block_text": "<TEXTE_ARABE_VOCALISE_ET_CORRIGE>"
    }
  ]
}

## TYPES DE BLOCS AUTORISES
- heading1 : Titres principaux de chapitre/section
- heading2 : Sous-titres et titres secondaires
- paragraph : Paragraphes de contenu principal
- list_item : Elements de liste ou enumeration
- header : En-tetes de page (nom livre, auteur)
- footer : Pieds de page (informations editoriales)
- page_number : Numerotation de page
- footnote : Notes de bas de page et commentaires

## REGLES DE TRAITEMENT

### VOCALISATION (Tashkeel)
- OBLIGATOIRE : Applique Tashkeel complet a tout texte arabe
- Precision : Utilise tes connaissances grammaticales pour une vocalisation correcte

### RECONSTRUCTION OCR
- Reassemble les mots/phrases fragmentes par l'OCR
- Corrige l'ordre logique des elements mal positionnes
- Preserve 100% du contenu original - aucune omission

### GESTION DES NOTES
- Identifie les appels de notes (123*) dans le texte principal
- Remplace par des exposants Unicode appropries
- Positionne correctement apres le mot concerne

### ESPACEMENT "و"
- Format : espace + و + espace pour une lisibilite optimale

### CRITERES DE REJET (keep_page: false)
- Pages completement vides ou illisibles
- Pages de couverture sans contenu textuel significatif
- Pages publicitaires ou promotionnelles sans rapport avec le livre
- Pages de copyright uniquement (sans autre contenu)

### CRITERES D'ACCEPTATION (keep_page: true)
- Toute page avec du contenu textuel arabe lisible
- Pages d'introduction, preface, ou presentation
- Pages de table des matieres ou index
- Pages avec au moins un paragraphe de texte coherent
- Pages de titre avec informations sur l'auteur ou l'oeuvre

IMPORTANT: Reponds UNIQUEMENT avec le JSON, sans aucun texte avant ou apres.

**Texte OCR a analyser:**
{{ocr_text}}
```
