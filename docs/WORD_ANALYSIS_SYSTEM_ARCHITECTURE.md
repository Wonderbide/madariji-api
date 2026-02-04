# Architecture du Système d'Analyse de Mots - Backcover

## Vue d'Ensemble

Le système d'analyse de mots de Backcover est conçu pour gérer efficacement l'analyse morphologique et la traduction de mots arabes avec une architecture optimisée pour éviter la duplication et supporter plusieurs langues et fournisseurs d'IA.

## 1. Flux de Traitement des Requêtes Word Detail

### Architecture de la requête
```
Client → POST /api/words/analyze → WordAnalysisController → DynamicWordAnalyzerService → AI Service (Gemini/OpenAI)
```

### Structure de la requête (`WordAnalysisRequest`)
```json
{
  "bookId": "550e8400-e29b-41d4-a716-446655440000",
  "pageNumber": 1,
  "wordInstanceId": "p0b0w5",  // Identifiant unique: page0, bloc0, mot5
  "wordText": "كتاب",
  "paragraphText": "هذا كتاب مفيد جداً للقراءة",
  "bookTitle": "Test Book",
  "targetLanguageCode": "fr"
}
```

### Réponse (`WordAnalysisDto`)
```json
{
  "id": "uuid",
  "word": "كِتَاب",  // Avec Tashkeel
  "type": "اسم",     // Nom, verbe, particule
  "translation": "livre",
  "root": "ك-ت-ب",
  "canonicalForm": "كتاب",
  "lexicalFields": ["éducation", "connaissance"],
  "details": {
    "genre": "masculin",
    "nombre": "singulier",
    // Autres détails morphologiques
  }
}
```

## 2. Architecture de Stockage en Base de Données

### Schéma de déduplication intelligent

```sql
dictionary_word (mots canoniques uniques)
├── id: UUID
├── canonical_form: "كتاب"
├── language_code: "ar"
└── UNIQUE(canonical_form, language_code)

word_analysis (analyses structurelles)
├── id: UUID
├── dictionary_word_id: FK
├── type: "اسم"
├── root: "ك-ت-ب"
├── details: JSONB
└── analysis_hash: SHA-256 (pour déduplication)

word_translation (traductions multilingues)
├── id: UUID
├── word_analysis_id: FK
├── language_code: "fr"
├── translation: "livre"
├── confidence_score: 0.95
└── UNIQUE(word_analysis_id, language_code, translation)

word_context (instances de mots dans les livres)
├── id: UUID
├── book_id: UUID
├── page_number: INTEGER
├── word_instance_id: "p0b0w5"
├── word_text_in_context: "كِتَابٌ" (avec diacritiques)
├── word_analysis_id: FK
├── word_translation_id: FK
├── paragraph_context_id: FK
└── UNIQUE(book_id, page_number, word_instance_id)

paragraph_context (contextes paragraphiques dédupliqués)
├── id: UUID
├── content_hash: SHA-256
├── content: TEXT
└── UNIQUE(content_hash)
```

## 3. Gestion des Mots Identiques dans Différentes Langues

### Stratégie multi-traduction

1. **Stockage par langue:** Chaque traduction est stockée avec son `language_code`
   ```sql
   -- Même mot, différentes traductions
   word_analysis_id | language_code | translation | confidence
   uuid-1          | fr           | livre       | 0.95
   uuid-1          | en           | book        | 0.98
   uuid-1          | es           | libro       | 0.92
   ```

2. **Sélection optimale:** Le système sélectionne la traduction avec le meilleur score de confiance pour la langue demandée

3. **Cache intelligent:** Les traductions sont mises en cache pour éviter les appels API répétés

## 4. Gestion des Mots Identiques dans Différents Contextes

### Mécanisme de contextualisation

1. **Identification unique par instance:**
   - Chaque occurrence de mot a un `wordInstanceId` unique (ex: "p0b0w5")
   - Permet de distinguer "كتاب" page 1 de "كتاب" page 50

2. **Réutilisation de l'analyse:**
   ```sql
   -- Deux instances du même mot partagent l'analyse
   word_context_1: {book_id: A, page: 1, word: "كتاب"} → word_analysis_1
   word_context_2: {book_id: A, page: 50, word: "كتاب"} → word_analysis_1
   ```

3. **Contexte paragraphique dédupliqué:**
   - Les paragraphes sont hashés (SHA-256) pour éviter la duplication
   - Permet de retrouver rapidement le contexte d'utilisation

## 5. Optimisations et Caractéristiques Avancées

### 1. Cache multicouche
- **Niveau 1:** Cache des analyses existantes par `wordInstanceId`
- **Niveau 2:** Cache des traductions par langue
- **Niveau 3:** Cache des analyses canoniques

### 2. Sélection dynamique du service IA
```java
// Configuration dans ai_flow_configuration
WORD_ANALYSIS → GOOGLE → gemini-2.0-flash-exp
// Peut être changé dynamiquement sans redémarrage
```

### 3. Traçabilité des coûts
- Chaque appel API est enregistré dans `prompt_usage`
- Permet l'analyse des coûts par modèle et par type de requête

### 4. Gestion des échecs (NO FALLBACK POLICY)
- Si le service configuré échoue → erreur explicite
- Pas de basculement automatique vers un autre service
- Garantit la prévisibilité du comportement

## 6. Cas d'Usage Concrets

### Exemple 1: Mot répété dans le même livre
```
Page 1: "هذا كتاب مفيد" → Analyse une fois, stocke
Page 50: "قرأت كتاب اليوم" → Réutilise l'analyse, nouveau contexte
```

### Exemple 2: Même mot, différentes langues cibles
```
Utilisateur 1 (français): "كتاب" → "livre"
Utilisateur 2 (anglais): "كتاب" → "book"
→ Deux traductions stockées, une seule analyse
```

### Exemple 3: Homographes avec contextes différents
```
"عين" (œil) dans contexte médical
"عين" (source) dans contexte géographique
→ Même forme canonique, analyses potentiellement différentes selon l'IA
```

## 7. Avantages de l'Architecture

Cette architecture garantit:
- **Efficacité:** Pas de duplication inutile
- **Scalabilité:** Support de millions de mots
- **Flexibilité:** Multi-langue et multi-provider
- **Traçabilité:** Suivi complet des coûts et performances
- **Performance:** Réutilisation maximale des analyses existantes
- **Cohérence:** Une seule source de vérité pour chaque analyse

## 8. Points Clés de l'Implémentation

1. **WordAnalysisController:** Point d'entrée REST API
2. **DynamicWordAnalyzerService:** Routeur vers le bon service IA
3. **WordAnalysisService (Gemini):** Implémentation Gemini
4. **OpenAIDetailedWordAnalyzerService:** Implémentation OpenAI
5. **AIModelConfigurationService:** Gestion de la configuration dynamique
6. **WordContextRepository:** Accès optimisé aux données avec requêtes natives

## 9. Configuration Actuelle

- **Service actif:** Gemini 2.0 Flash (gemini-2.0-flash-exp)
- **Langues supportées:** fr, en, es, de, ar
- **Cache activé:** Oui
- **Déduplication:** Automatique via hash
- **NO FALLBACK POLICY:** Activée - échec explicite sans basculement

Cette documentation représente l'état actuel du système d'analyse de mots, qui est déjà optimisé pour gérer efficacement les cas d'usage identifiés.