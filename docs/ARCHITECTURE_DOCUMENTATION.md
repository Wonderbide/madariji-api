# Backcover - Documentation Architecturale Complète

## Vue d'ensemble du système

Backcover est une plateforme de lecture interactive pour textes arabes avec analyse linguistique IA. Le système comprend :
- **Backend** : API REST Spring Boot avec services d'IA (Gemini, OpenAI)
- **Frontend** : Application React TypeScript avec interface de lecture avancée
- **IA** : Analyse morphologique et traduction contextuelle multilingue

---

## 🔄 FLOWS UTILISATEUR COMPLETS

### 1. **FLOW D'AUTHENTIFICATION**

```
📱 Frontend                    🔐 Auth0                     🖥️ Backend
    │                           │                           │
    ├─1. Clic "Se connecter"───→│                           │
    │                           ├─2. Formulaire login      │
    │                           ├─3. Validation           │
    ├─4. Récupération JWT ←─────┤                           │
    │                           │                           │
    ├─5. POST /api/users/sync ─┼─────────────────────────→ │
    │    (JWT token)             │                           ├─6. Création/MAJ User
    │                           │                           ├─7. Retour UserDto
    ├─8. Redirection /my-library ←─────────────────────────┤
```

**Détails techniques :**
- JWT token stocké dans Auth0 context
- Synchronisation automatique avec la base locale
- Création de UserSettings par défaut
- Gestion des erreurs avec retry automatique

### 2. **FLOW DE TÉLÉCHARGEMENT ET TRAITEMENT DE LIVRE**

```
📚 Upload                      🔄 Processing                📊 Status
    │                           │                           │
    ├─1. Sélection PDF ─────────┼─────────────────────────→ │
    ├─2. POST /api/books/upload │                           │
    │   (multipart/form-data)    ├─3. Validation PDF         │
    │                           ├─4. Extraction couverture  │
    │                           ├─5. Upload GCS bucket      │
    │                           ├─6. Lancement Vision API   │
    │                           │   (par batches 100 pages) │
    │                           │                           │
    ├─7. Polling GET /progress ←┼─8. BatchStructuredEvent   │
    │   (toutes les 5s)         │   (traitement progressif) │
    │                           ├─9. Enrichissement IA     │
    │                           │   (structuration pages)   │
    │                           ├─10. Sauvegarde JSON       │
    ├─11. Status: COMPLETED ←───┤    (incremental)          │
```

**Système de récupération automatique :**
- Détection des livres partiellement traités au démarrage
- Reprise automatique depuis la dernière page réussie
- Calcul mathématique : `batchIndex = pageNumber / batchSize`

### 3. **FLOW DE LECTURE INTERACTIVE**

```
📖 Page Display               🔍 Word Analysis             💾 Word Lists
    │                           │                           │
    ├─1. GET /books/{id}/structure                         │
    ├─2. Rendu blocs enrichis  │                           │
    │                           │                           │
    ├─3. Clic sur mot ─────────→│                           │
    │                           ├─4. Vérification cache    │
    │                           │   (par wordInstanceId)   │
    │                           │                           │
    │                           ├─5. POST /api/words/analyze │
    │                           │   (si cache MISS)         │
    │                           │   {                       │
    │                           │     wordText,             │
    │                           │     paragraphText,        │
    │                           │     bookTitle,            │
    │                           │     targetLanguage        │
    │                           │   }                       │
    │                           │                           │
    │                           ├─6. Analyse Gemini/OpenAI │
    │                           │   • Type grammatical      │
    │                           │   • Forme canonique       │
    │                           │   • Traduction littérale  │
    │                           │   • Sens contextuel       │
    │                           │   • Détails morphologiques│
    │                           │                           │
    ├─7. Affichage WordDetails ←┤                           │
    │   (panneau latéral)       │                           │
    │                           │                           │
    ├─8. Clic "Ajouter à liste"─┼─────────────────────────→ │
    │                           │                           ├─9. POST /wordlist/items
    │                           │                           ├─10. Sauvegarde avec
    │                           │                           │    analysisId lié
    ├─11. MAJ interface ←──────────────────────────────────┤
```

### 4. **FLOW DE GESTION DES LISTES DE MOTS**

```
📚 Book-Specific Lists        🌐 Default Lists             🔗 Analysis Integration
    │                           │                           │
    ├─1. GET /wordlist/book/{id}/items/details             │
    │   (mots du livre courant) │                           │
    │                           │                           │
    │                           ├─2. GET /wordlist/items/details
    │                           │   (liste globale)         │
    │                           │                           │
    ├─3. Ajout mot avec contexte┼─────────────────────────→ │
    │   POST /wordlist/book/{id}/items                      │
    │   {                       │                           │
    │     wordText,             │                           │
    │     translatedText,       │                           │
    │     bookId,               │                           │
    │     pageNumber,           │                           │
    │     context               │                           │
    │   }                       │                           │
    │                           │                           │
    │                           │                           ├─4. Liaison analysis
    │                           │                           │   PUT /items/{itemId}/analysis/{analysisId}
    │                           │                           │
    ├─5. Affichage WordTable ←──┼─────────────────────────────┤
    │   (avec détails complets) │                           │
```

### 5. **FLOW DE CHANGEMENT DE LANGUE**

```
🌍 Language Selection         🔄 Cache Invalidation        🔍 Re-analysis
    │                           │                           │
    ├─1. Sélection nouvelle langue                         │
    │   (LanguageSelector)      │                           │
    │                           │                           │
    ├─2. MAJ LanguageContext ──→├─3. Invalidation cache     │
    │   localStorage update     │   (par langue)            │
    │                           │                           │
    │                           │                           ├─4. Nouvelle analyse
    │                           │                           │   pour mots visibles
    │                           │                           │   POST /words/analyze
    │                           │                           │   (targetLanguage: nouvelle)
    │                           │                           │
    ├─5. Re-rendu interface ←───┼─────────────────────────────┤
    │   (nouvelles traductions) │                           │
```

---

## 🏗️ ARCHITECTURE BACKEND DÉTAILLÉE

### Services de Traitement IA

#### **GeminiAnalysisService**
- **Purpose** : Structuration de pages et analyse de mots
- **Token Limits** : 16384 tokens max output
- **Logging** : Comptage détaillé des tokens
- **Version** : Gemini 2.0 Flash Exp

#### **OpenAIDetailedWordAnalyzerService** 
- **Purpose** : Alternative pour analyse de mots
- **Model** : GPT-4 Turbo
- **Temperature** : 0.05 (très déterministe)
- **Max Tokens** : 4096

#### **PromptTemplateService**
- **Langues supportées** : 6 (FR, EN, AR, ES, DE, IT)
- **Distinction critique** : `translation` (littéral) vs `meaning` (contextuel)
- **Pas d'exemples** : Templates génériques pour éviter les biais

### Pipeline de Traitement Progressif

#### **Phase 1 : Vision API**
```java
// Traitement par batches de 100 pages
visionBatchSize = 100
totalBatches = totalPages / batchSize
for (batch in batches) {
    publishEvent(new BatchStructuredEvent(bookId, batchIndex))
}
```

#### **Phase 2 : Enrichissement IA**
```java
// Traitement batch par batch
@EventListener
handleBatchStructured(BatchStructuredEvent event) {
    // Récupération pages du batch
    // Structuration avec IA
    // Sauvegarde incrémentale JSON
    // MAJ lastSuccessfullyProcessedPageIndex
}
```

#### **Phase 3 : Récupération Crash**
```java
// Calcul automatique des batches manquants
int lastPage = book.getLastSuccessfullyProcessedPageIndex();
int nextBatch = (lastPage + 1) / batchSize;
// Republication des événements manquants
```

### Nouvelle Architecture de Traduction

#### **Tables principales :**
- **word_context** : Instances de mots avec position exacte
- **word_analysis** : Analyses linguistiques (partagées)
- **word_translation** : Traductions par langue/service
- **paragraph_context** : Contextes de paragraphes

#### **Déduplication intelligente :**
- Une seule analyse par forme canonique
- Traductions multiples par service IA
- Partage d'analyses entre contextes

---

## 🎨 ARCHITECTURE FRONTEND DÉTAILLÉE

### Gestion d'État Globale

#### **LanguageContext**
```typescript
interface LanguageContextType {
  targetLanguage: string;           // Code langue (fr, en, es...)
  setTargetLanguage: (lang: string) => void;
  supportedLanguages: Language[];   // Liste complète
}
```

#### **DisplaySettingsContext**
```typescript
interface Settings {
  theme: 'light' | 'dark' | 'sepia';
  fontSize: 'small' | 'medium' | 'large';
  fontFamily: string;
  showWordDetails: boolean;
  showWordList: boolean;
  showTimer: boolean;
}
```

### Hooks Spécialisés

#### **useWordInteractionManager**
```typescript
const {
  selectedWord,          // Mot actuellement sélectionné
  wordDetails,          // Détails d'analyse
  consultedWords,       // Cache global des mots
  isAnalyzing,          // État de chargement
  handleWordClick,      // Gestionnaire principal
  addToWordList,        // Ajout à liste
  clearSelection       // Réinitialisation
} = useWordInteractionManager(bookId, targetLanguage);
```

**Fonctionnalités avancées :**
- Cache global avec invalidation par langue
- Debouncing des requêtes
- Gestion optimiste des états
- Retry automatique sur erreur

#### **useBookData**
```typescript
const {
  bookData,            // Métadonnées du livre
  pageStructure,       // Structure enrichie des pages
  currentPage,         // Page actuelle
  totalPages,          // Nombre total de pages
  isLoading,           // État de chargement
  error,               // Gestion d'erreurs
  setCurrentPage      // Navigation
} = useBookData(bookId);
```

### Composants de Rendu Avancés

#### **Block Components**
```typescript
// Système modulaire pour différents types de contenu
<ParagraphBlock 
  content={block.content}
  onWordClick={handleWordClick}
  selectedWordId={selectedWord?.wordInstanceId}
/>

<HeadingBlock level={block.level} content={block.content} />
<FootnoteBlock reference={block.reference} content={block.content} />
```

#### **Navigation Patterns**
- **Book Mode** : Navigation page par page avec animations
- **Scroll Mode** : Défilement continu avec suivi automatique
- **Keyboard/Touch** : Support complet des gestes et raccourcis

---

## 📊 INVENTAIRE TECHNIQUE COMPLET

### Backend - Spring Boot

#### **Controllers (8)**
1. **BookController** - CRUD livres, upload, structure
2. **UserController** - Sync Auth0, mots consultés
3. **WordAnalysisController** - Analyse IA de mots
4. **UserWordListController** - Gestion listes de mots
5. **ReadingActivityController** - Progression lecture
6. **UserSettingsController** - Préférences utilisateur
7. **LanguageController** - Configuration langues
8. **BookRecoveryController** - Récupération automatique

#### **Services (18)**
- **Traitement** : BookProcessingService, VisionResultProcessingService
- **IA** : GeminiAnalysisService, OpenAIDetailedWordAnalyzerService, WordAnalysisService
- **Stockage** : LocalStorageService, GcsStorageService
- **Récupération** : BookRecoveryService, ScheduledRecoveryService
- **Métier** : UserService, UserWordListService, ReadingActivityService, UserSettingsService
- **Utilitaires** : PromptTemplateService, TranslationContextService
- **Polling** : VisionPollingService, OcrInitiationService

#### **Repositories (12)**
- Book, User, DictionaryWord, WordAnalysis
- WordContext, WordTranslation, ParagraphContext
- UserWordList, UserWordListItem, UserSettings
- UserBookProgress, ContextualWordMeaning

#### **Configuration (7)**
- WordAnalysisConfig, PageStructuringConfig, LanguageConfig
- GcpConfig, SecurityConfig, SchedulingConfig, Config

### Frontend - React TypeScript

#### **Pages (4)**
- **PublicHomePage** - Landing page
- **LoginPage** - Authentification
- **HomePage** - Bibliothèque utilisateur
- **ReadingPage** - Interface de lecture

#### **Components (20+)**
- **Core** : Page, ScrollPage, WordDetails, WordTable
- **Navigation** : Header, BurgerMenu, Navigation
- **UI** : BookCard, LanguageSelector, SettingsPanel
- **Blocks** : ParagraphBlock, HeadingBlock, FootnoteBlock
- **Modals** : BookUploadModal
- **Panels** : MobileOptionsPanel
- **Utilities** : StatusIndicator, Spinner, RecoveryButton

#### **Hooks (8)**
- **API** : useApi, useApiUrl
- **State** : useWordInteractionManager, useBookData
- **Features** : useConsultedWords, useBookProgress
- **Config** : useSupportedLanguages
- **Performance** : useOptimizedPolling

#### **Context (2)**
- **LanguageContext** - Gestion multilingue
- **DisplaySettingsContext** - Préférences d'affichage

---

## 🔧 POINTS D'INTÉGRATION CRITIQUES

### API Authentication
```typescript
// JWT automatique dans tous les appels
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};
```

### Real-time Updates
```typescript
// Polling optimisé pour statut de traitement
const { progress, status } = useOptimizedPolling(
  `/api/books/${bookId}/progress`,
  5000, // 5s interval
  status !== 'COMPLETED'
);
```

### Caching Strategy
```typescript
// Cache global avec invalidation intelligente
const cacheKey = `${wordInstanceId}-${targetLanguage}`;
if (cache.has(cacheKey) && !cache.isExpired(cacheKey)) {
  return cache.get(cacheKey);
}
```

### Error Boundaries
```typescript
// Gestion d'erreurs granulaire
<ErrorBoundary fallback={<ErrorFallback />}>
  <WordAnalysisPanel />
</ErrorBoundary>
```

---

## 🚀 CARACTÉRISTIQUES TECHNIQUES AVANCÉES

### Performance
- **Lazy Loading** : Chargement différé des composants
- **Memoization** : React.memo, useMemo, useCallback
- **Debouncing** : 300ms pour scroll tracking, 500ms pour sauvegarde
- **Cache Management** : Invalidation intelligente par timestamp

### Accessibilité
- **ARIA Labels** : Support complet screen readers
- **Keyboard Navigation** : Raccourcis clavier complets
- **RTL Support** : Rendu correct texte arabe
- **High Contrast** : Thèmes accessibles

### Internationalisation
- **Multi-langue UI** : Interface en français avec support extensible
- **Target Languages** : 9 langues de traduction pour textes arabes
- **Locale Management** : Formatage dates, nombres par locale
- **Font Support** : Polices optimisées pour texte arabe

### Sécurité
- **JWT Validation** : Vérification côté backend
- **CORS Configuration** : Politique stricte
- **Input Sanitization** : Protection XSS
- **File Upload Security** : Validation types MIME

Ce système représente une architecture moderne et robuste pour le traitement et l'analyse de textes arabes avec des capacités d'IA avancées et une expérience utilisateur optimisée.

---

## 📝 MAINTENANCE DE LA DOCUMENTATION

### Fichiers de documentation
- **`ARCHITECTURE_DOCUMENTATION.md`** - Ce document (architecture complète)
- **`SYSTEM_FLOWS_DIAGRAM.md`** - Diagrammes visuels Mermaid
- **`CHANGELOG.md`** - Historique détaillé des fonctionnalités avec commits

### Processus de mise à jour
1. **Nouvelle fonctionnalité** → Mise à jour `CHANGELOG.md` avant commit
2. **Architecture change** → Mise à jour de ce document
3. **Nouveau flow** → Ajout diagramme dans `SYSTEM_FLOWS_DIAGRAM.md`
4. **Commit** → Référencer le changelog dans le message

### Template commit avec documentation
```bash
# 1. Développement de la fonctionnalité
git checkout -b feature/nouvelle-fonctionnalite

# 2. Mise à jour changelog avant commit
# Éditer CHANGELOG.md avec détails de la fonctionnalité

# 3. Commit avec référence changelog
git add .
git commit -m "feat(scope): description

Voir CHANGELOG.md section [FEAT] Titre - [Date] pour détails complets"

# 4. Si changement architectural majeur
# Mettre à jour ARCHITECTURE_DOCUMENTATION.md

# 5. Merge et tag si version majeure
git checkout main
git merge feature/nouvelle-fonctionnalite
git tag -a v1.x.x -m "Version avec fonctionnalité X"
```

Cette approche garantit la traçabilité complète entre code, commits et documentation.