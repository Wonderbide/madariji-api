-- Update WORD_ANALYSIS_V1 prompt to include book metadata (genre, author, description)
UPDATE prompt_library
SET prompt_text = '**Tâche :** Analyse grammaticalement et sémantiquement le mot arabe cible fourni, en utilisant **impérativement le contexte de paragraphe fourni** pour déterminer le sens et la traduction les plus précis dans la langue demandée (%s).

**Contexte Fourni :**
*   **Mot Cible :** %s
*   **Titre du Livre :** %s
*   **Genre du Livre :** %s
*   **Auteur :** %s
*   **Description du Livre :** %s
*   **Contexte de Paragraphe (extrait de la page actuelle) :**
--- DEBUT CONTEXTE ---
%s
--- FIN CONTEXTE ---
*   **Langue de traduction demandée :** %s

**Instructions Générales :**
1.  Concentre ton analyse **exclusivement sur le "Mot Cible"** fourni ci-dessus (`%s`).
2.  **CRITIQUE** : Lis attentivement le "Contexte de Paragraphe" pour comprendre si le mot fait partie d''une expression idiomatique. Cela influence UNIQUEMENT le champ "meaning", PAS le champ "translation".
3.  **IMPORTANT** : Utilise les métadonnées du livre (genre, auteur, description) pour orienter ton analyse vers le domaine thématique approprié. Par exemple, dans un livre de poésie soufie, le mot "قلب" (cœur) aura un sens mystique/spirituel plutôt que littéral.
4.  Détermine le type grammatical principal du "Mot Cible" ("اسم", "فعل", "حرف", ou "unknown").
    **RÈGLE IMPORTANTE POUR LES HARFS**: Si le mot est annexé à un harf (comme بـ, لـ, كـ, فـ, وـ), le mot principal doit être considéré comme un "اسم" (ism) et non comme un "حرف" (harf).

**CHAMPS LINGUISTIQUES OBLIGATOIRES :**

5.  **jidar** (الجذر المجرد) : La forme nue du mot, dévêtue de toutes les particules annexées :
    - Retirer l''article défini (ال)
    - Retirer les prépositions attachées (بـ, لـ, كـ, فـ, وـ)
    - Retirer les pronoms possessifs/suffixes (-ه, -ها, -هم, -ك, -كم, -ي, -نا)
    - Retirer le tanwin (ً, ٍ, ٌ)
    - Garder les voyelles courtes (harakat) pour indiquer la vocalisation correcte
    - Pour les verbes : la forme passée 3ème personne masculin singulier (هو) avec tashkeel

6.  **root** (الجذر الثلاثي) : La racine trilitère ou quadrilitère avec espaces entre les lettres.
    - Exemples : "ك ت ب" pour كتاب, "ع ل م" pour علم, "د ر س" pour درس
    - Pour les racines quadrilitères : "ت ر ج م" pour ترجمة
    - Si pas de racine applicable (particules) : "-"

7.  **masdar** (المصدر) : Le nom d''action associé au mot :
    - Pour les verbes : le masdar du verbe (ex: كِتَابَة pour كَتَبَ)
    - Pour les noms dérivés de verbes : le masdar du verbe source
    - Pour les noms primitifs ou particules : null

8.  **wazn** (الوزن) : Le schème/pattern morphologique :
    - Pour les verbes : le wazn à la forme هو passée (فَعَلَ, فَعَّلَ, فَاعَلَ, أَفْعَلَ, تَفَعَّلَ, تَفَاعَلَ, اِنْفَعَلَ, اِفْتَعَلَ, اِفْعَلَّ, اِسْتَفْعَلَ)
    - Pour les noms : le wazn nominal (فِعَال, فَعِيل, مَفْعُول, فَاعِل, فُعْلَة, etc.)
    - Pour les particules : null

9.  Remplis l''objet `details` selon le type grammatical du "Mot Cible" :
    *   Si "اسم" : clés `"meaning"` (sens contextuel précis), `"singular"`, `"dual"`, `"plural"`.
    *   Si "فعل" : clés `"meaning"` (sens contextuel précis), `"madi"`, `"mudari"`, `"amr"`.
    *   Si "حرف" : clés `"meaning"` (sens contextuel précis), `"function"` (rôle grammatical).
    *   Si "unknown" : `details` est `null`.

10. **TRADUCTION DYNAMIQUE** : Fournis la traduction du "Mot Cible" dans la langue spécifiée (%s). TOUJOURS donner la traduction du mot seul, comme dans un dictionnaire.

11. **DISTINCTION ABSOLUE entre "translation" et "meaning"** :
    - "translation" : UNIQUEMENT la traduction du mot isolé (dictionnaire)
    - "meaning" : Le sens du mot dans ce contexte précis (inclut expressions idiomatiques)

12. **Identifie les champs lexicaux** : Fournis de 1 à 3 champs lexicaux dans `"lexical_fields"`.

**FORMAT DE SORTIE JSON STRICT :**
1.  Ta réponse doit être **UNIQUEMENT** un objet JSON valide. Pas de texte avant/après. Pas de ```json.
2.  Structure JSON attendue :
    ```json
    {
      "word": "VALEUR_EXACTE_DU_MOT_CIBLE_FOURNI",
      "type": "TYPE_TROUVÉ",
      "translation": "TRADUCTION_DANS_LA_LANGUE_DEMANDÉE",
      "jidar": "FORME_NUE_VOCALISÉE",
      "root": "ج ذ ر",
      "masdar": "المصدر" | null,
      "wazn": "الوزن" | null,
      "details": { /* OBJET DÉTAILS SELON TYPE */ } | null,
      "lexical_fields": ["champ1", "champ2"]
    }
    ```
3.  **RÈGLE ABSOLUMENT CRITIQUE : La valeur de la clé `"word"` DOIT ÊTRE EXACTEMENT LA MÊME que le "Mot Cible" fourni (`%s`).**

**Exécute maintenant l''analyse pour le "Mot Cible" : %s**',
    updated_at = NOW()
WHERE prompt_key = 'WORD_ANALYSIS_V1' AND is_active = true;
