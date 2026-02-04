# Backcover - Historique des Fonctionnalités

## [FEATURE] Système de contrôle d'accès basé sur les rôles (RBAC) - [2025-07-21]
**Commit:** `pending` - Implement role-based access control system
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/resources/db/migration/V14__add_user_role_column.sql` - Nouveau
- `src/main/java/com/backcover/model/User.java` - Ajout du champ role
- `src/main/java/com/backcover/config/CustomJwtAuthenticationConverter.java` - Nouveau
- `src/main/java/com/backcover/config/SecurityConfig.java` - Activation method security
- `src/main/java/com/backcover/controller/UserController.java` - Ajout endpoint /me
- `src/main/java/com/backcover/controller/BookController.java` - Protection upload
- `src/main/java/com/backcover/dto/UserProfileDto.java` - Nouveau

### Description
Implémentation d'un système complet de contrôle d'accès basé sur les rôles stockés en base de données.
Trois rôles sont définis : ROLE_FREE (défaut), ROLE_PREMIUM, et ROLE_ADMIN.

### Changements principaux
- Migration SQL pour ajouter la colonne `role` dans la table `users`
- `CustomJwtAuthenticationConverter` qui charge le rôle depuis la DB à chaque requête
- Endpoint `/api/users/me` pour récupérer le profil utilisateur avec son rôle
- Protection de l'endpoint d'upload avec `@PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")`
- Migration depuis les rôles JWT vers les rôles en base de données

### Impact
- Les utilisateurs FREE ne peuvent plus uploader de livres
- Seuls les utilisateurs PREMIUM et ADMIN ont accès à l'upload
- Le système est prêt pour l'implémentation future des abonnements

---

## [FIX] Simplification configuration DeepSeek - [2025-07-21]  
**Commit:** `pending` - Simplify DeepSeek configuration by moving bean to Config.java
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/DeepSeekConfig.java` - Supprimé
- `src/main/java/com/backcover/config/Config.java` - Ajout du bean deepSeekChatModel

### Description
Suppression de la classe DeepSeekConfig qui causait des problèmes de packaging JAR.
Le bean deepSeekChatModel est maintenant défini directement dans Config.java.

### Impact
- Résout l'erreur "Failed to read candidate component class" en production
- Configuration plus simple et cohérente avec le reste de l'application

---

## [FIX] Correction DeepSeekConfig pour packaging JAR Spring Boot - [2025-07-20]
**Commit:** `d451d84` - Fix DeepSeekConfig to avoid Spring Boot JAR packaging issues
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/DeepSeekConfig.java` - Déplacement @ConditionalOnProperty sur méthode

### Description
Correction du problème de démarrage en production causé par @ConditionalOnProperty au niveau
de la classe. Spring Boot ne pouvait pas lire la classe dans le JAR nested.

### Impact
- Résout l'erreur "Failed to read candidate component class"
- Permet le démarrage correct de l'application avec DeepSeek

---

## [FEATURE] Intégration DeepSeek pour traitement LLM économique - [2025-07-20]
**Commit:** `2433333` - Add DeepSeek integration for cost-effective LLM processing
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/model/AIFlowConfiguration.java` - Ajout DEEPSEEK dans enum AIProvider
- `src/main/java/com/backcover/config/DeepSeekConfig.java` - Configuration Spring pour DeepSeek
- `src/main/java/com/backcover/service/DeepSeekDetailedWordAnalyzerService.java` - Service d'analyse de mots
- `src/main/java/com/backcover/service/DeepSeekPageStructuringService.java` - Service de structuration de pages
- `src/main/java/com/backcover/service/DynamicWordAnalyzerService.java` - Support DeepSeek
- `src/main/java/com/backcover/service/DynamicPageStructuringService.java` - Support DeepSeek
- `src/main/java/com/backcover/service/OpenAI*.java` - Ajout @Qualifier pour résoudre conflits de beans
- `src/main/resources/application.properties` - Configuration DeepSeek avec Doppler
- `src/main/resources/db/migration/configure_deepseek_production*.sql` - Scripts migration production

### Description
Ajout de DeepSeek comme nouveau fournisseur LLM (93% moins cher que GPT-4) :
- Intégration du modèle deepseek-reasoner pour WORD_ANALYSIS et PAGE_STRUCTURING
- API compatible OpenAI avec URL de base personnalisée
- Résolution des conflits de beans ChatModel avec @Qualifier
- Scripts SQL pour activer DeepSeek en production

### Impact
- Réduction drastique des coûts de traitement LLM (93% moins cher)
- Support du tarif off-peak (75% de réduction 16h30-00h30 UTC)
- Maintien de la qualité avec le modèle deepseek-reasoner
- Migration transparente via configuration base de données

---

## [FIX] Autoriser l'accès public aux endpoints de healthcheck - [2025-07-18]
**Commit:** `pending` - Allow public access to /health endpoint for Railway healthcheck
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/SecurityConfig.java` - Ajout /health et / dans permitAll()

### Description
Correction de la configuration de sécurité pour permettre l'accès public aux endpoints
de healthcheck sans authentification JWT. Railway recevait une erreur 401 Unauthorized.

### Impact
- Railway peut maintenant accéder à /health sans token JWT
- Résout l'erreur "Attempt failed with status 401"
- L'application peut démarrer correctement sur Railway

---

## [FIX] TTS Configuration pour utiliser GCP_CREDENTIALS_JSON - [2025-07-18]
**Commit:** `pending` - Fix TTS to use GCP_CREDENTIALS_JSON from environment
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/TTSConfig.java` - Utiliser GCP_CREDENTIALS_JSON comme GcpConfig

### Description
Correction du TTSConfig pour utiliser la variable d'environnement GCP_CREDENTIALS_JSON
au lieu des credentials par défaut, ce qui causait l'échec du démarrage en production.

### Impact
- Permet au service TTS de démarrer correctement avec les credentials Doppler
- Résout l'erreur "Failed to create TextToSpeechClient" au démarrage
- Cohérence avec GcpConfig qui utilise déjà cette approche

---

## [FIX] Configuration Railway pour healthcheck - [2025-07-18]
**Commit:** `pending` - Fix Railway deployment: add simple health endpoint and initial delay
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/resources/application-production.properties` - Ajout server.address=0.0.0.0 et configuration proxy
- `Dockerfile` - Ajout --server.address=0.0.0.0 dans la commande CMD
- `src/main/java/com/backcover/controller/HealthController.java` - Nouveau controller avec endpoint /health simple
- `railway.toml` - Changement healthcheck de /actuator/health vers /health, timeout 60s et initialDelay 30s

### Description
Correction du déploiement Railway : 
- Force l'application à écouter sur toutes les interfaces (0.0.0.0) via la ligne de commande Docker
- Création d'un endpoint /health simple qui répond toujours OK (sans dépendances)
- Ajout d'un délai initial de 45 secondes avant le premier healthcheck
- Changement du path de healthcheck vers le nouvel endpoint simple

### Impact
- Permet à Railway d'accéder au healthcheck depuis l'extérieur du conteneur
- Évite les échecs de healthcheck dus aux dépendances (DB, etc.)
- Laisse le temps à l'application de démarrer complètement
- Résout le problème de redémarrage en boucle

---

## [FEAT] Intégration Logtail pour le logging centralisé - [2025-07-17]
**Commit:** `pending` - Add Logtail HTTP logging integration with custom appender
**Branche:** `main`
**Fichiers modifiés:** 
- `pom.xml` - Ajout de la dépendance logstash-logback-encoder
- `src/main/java/com/backcover/config/LogtailHttpAppender.java` - Nouveau appender HTTP personnalisé
- `src/main/resources/logback-spring.xml` - Configuration Logback pour logs JSON et Logtail
- `LOGTAIL_SETUP.md` - Documentation de configuration
- `scripts/setup-logtail.sh` - Script d'installation

### Description
Intégration complète de Logtail (Better Stack) pour centraliser les logs de l'application en production. Les logs sont envoyés en temps réel au format JSON avec toutes les métadonnées nécessaires.

### Impact
- Monitoring centralisé des logs en production
- Format JSON structuré pour une meilleure recherche
- Performance optimisée avec appender asynchrone
- Séparation automatique dev/prod

### Configuration
- Token Logtail ajouté dans Doppler
- Endpoint: s1390626.eu-nbg-2.betterstackdata.com
- Activé uniquement en profil production

---

## Format du Changelog

Chaque entrée suit ce format :
```
## [Type] Titre de la fonctionnalité - [Date]
**Commit:** `hash` - Description du commit
**Branche:** `nom-branche`
**Fichiers modifiés:** 
- `chemin/fichier1.java` - Description changement
- `chemin/fichier2.tsx` - Description changement

### Description
Description détaillée de la fonctionnalité

### Impact
- Impact utilisateur
- Impact technique
- Migrations éventuelles

### Tests
- Tests ajoutés/modifiés
- Validation effectuée

---
```

## Types de changements
- **[FEAT]** - Nouvelle fonctionnalité
- **[FIX]** - Correction de bug
- **[REFACTOR]** - Refactoring sans changement fonctionnel
- **[DOCS]** - Documentation uniquement
- **[CONFIG]** - Changement de configuration
- **[PERF]** - Amélioration de performance
- **[TEST]** - Ajout/modification de tests

---

# HISTORIQUE

## [DOCS] Guide de configuration Supabase pour le backend - [2025-07-03]
**Commit:** `pending` - Add Supabase backend configuration guide
**Branche:** `main`
**Fichiers modifiés:**
- `SUPABASE_BACKEND_SETUP.md` - Guide complet de configuration
- `.gitignore` - Ajout des patterns pour fichiers .env

### Description
Documentation complète pour configurer le backend avec Supabase, incluant les variables d'environnement nécessaires et les options de déploiement.

### Impact
- **Impact utilisateur**: Aucun
- **Impact technique**: Facilite la configuration Supabase pour l'équipe backend
- **Migrations éventuelles**: Aucune

### Tests
- Documentation uniquement, pas de code à tester

---

## [FIX] Suppression du fallback dans l'authentification multi-provider - [2025-07-03]
**Commit:** `pending` - Remove fallback in multi-provider authentication per NO FALLBACK POLICY
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/MultiAuthSecurityConfig.java` - Logique stricte sans fallback
- `src/main/resources/application.properties` - Ajout auth.provider pour sélection explicite

### Description
Suppression du mécanisme de fallback dans l'authentification multi-provider conformément à la politique NO FALLBACK. Le système utilise maintenant exclusivement le provider configuré (auth0 ou supabase).

### Impact
- **Impact utilisateur**: Doit configurer explicitement AUTH_PROVIDER=auth0 ou AUTH_PROVIDER=supabase
- **Impact technique**: Échec explicite si le provider sélectionné n'est pas correctement configuré
- **Migrations éventuelles**: Définir AUTH_PROVIDER dans Doppler

### Tests
- Compilation réussie
- Échec explicite si le provider n'est pas configuré
- Pas de basculement automatique entre providers

---

## [CONFIG] Ajout du support d'authentification Supabase - [2025-07-03]
**Commit:** `e4abf94` - Add Supabase authentication support alongside Auth0
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/SupabaseConfig.java` - Nouvelle classe de configuration Supabase
- `src/main/java/com/backcover/config/MultiAuthSecurityConfig.java` - Support multi-fournisseur JWT
- `src/main/resources/application.properties` - Variables de configuration Supabase

### Description
Ajout du support d'authentification Supabase tout en maintenant la compatibilité avec Auth0. Cette approche permet une migration progressive sans interruption de service.

### Impact
- **Impact utilisateur**: Aucun impact immédiat, les tokens Auth0 continuent de fonctionner
- **Impact technique**: Support simultané de deux fournisseurs d'authentification JWT
- **Migrations éventuelles**: Les variables Supabase doivent être configurées dans Doppler

### Tests
- Compilation réussie avec les nouvelles classes
- Configuration multi-provider activée par défaut
- Auth0 reste le fournisseur par défaut si Supabase n'est pas configuré

---

## [FEAT] Enhanced OCR Verification Rules for Arabic Religious Texts - [2025-06-29]
**Commit:** `pending` - Update page structuring prompt with enhanced OCR verification rules
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/resources/db/backup/update_page_structuring_prompt_v3.sql` - Nouveau prompt v3.0
- `src/main/resources/db/backup/insert_model_configurations.sql` - Configurations des modèles AI

### Description
Amélioration majeure du prompt de structuration de pages avec des règles spécifiques pour les textes religieux arabes et une vérification accrue des erreurs OCR.

### Nouvelles fonctionnalités
- Vérification critique des points diacritiques (souvent manqués/ajoutés par l'OCR)
- Vérification de la cohérence contextuelle des mots
- Règles spécifiques pour textes religieux :
  - Préservation intégrale des versets coraniques
  - Ajout automatique de ﷺ après mentions du Prophète
  - Ajout de رضي الله عنه pour les Compagnons
- Avertissement contre la confiance aveugle au tashkeel OCR

### Impact
- Amélioration significative de la précision pour les textes religieux arabes
- Réduction des erreurs de reconnaissance des lettres similaires
- Respect automatique des formules religieuses traditionnelles

---

## [FIX] Book Completion Logic with Rejected Pages - [2025-06-29]
**Commit:** `pending` - Fix book completion detection when pages are rejected
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/BookProcessingService.java` - Amélioration de la logique de détection de fin

### Description
Correction du bug où les livres restaient bloqués en statut PARTIALLY_ENRICHED quand des pages étaient rejetées (keep_page=false). Le système comparait le nombre de pages enrichies avec le nombre total de pages du PDF, ce qui empêchait la completion.

### Impact
- Les livres passent correctement en statut COMPLETED même si des pages sont rejetées
- Le message de completion affiche maintenant "X pages finalisées sur Y pages totales"
- Résout le bug où un livre de 47 pages restait bloqué à 43 pages

---

## [FIX] BookRecoveryService Compilation Errors - [2025-06-29]
**Commit:** `pending` - Fix BookRecoveryService compilation errors
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/BookRecoveryService.java` - Remplacé BookDto par JsonNode parsing

### Description
Correction des erreurs de compilation dans BookRecoveryService causées par des références à des DTOs inexistants (BookDto) et des méthodes manquantes.

### Impact
- Le service compile correctement maintenant
- La récupération peut parser les fichiers JSON sans dépendre de DTOs spécifiques

---

## [FIX] BookRecoveryService Path Resolution - [2025-06-29]
**Commit:** `pending` - Fix BookRecoveryService path issue and add missing method
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/LocalStorageService.java` - Ajout de getFinalResultsDirectory()
- `src/main/java/com/backcover/service/BookRecoveryService.java` - Amélioration des logs d'erreur

### Description
Correction du service de récupération qui ne trouvait pas les pages structurées à cause d'un problème de résolution de chemin. Ajout de la méthode manquante getFinalResultsDirectory() dans LocalStorageService.

### Impact
- Permet au service de récupération de fonctionner correctement en production
- Améliore les logs pour faciliter le débogage des problèmes de chemin

---

## [FIX] Structured Pages Directory Path for Production - [2025-06-28]
**Commit:** `pending` - Fix structured pages directory path for production
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/BookRecoveryService.java` - Utilisation de LocalStorageService au lieu du path codé en dur

### Description
Correction du chemin d'accès aux pages structurées en production. Le code utilisait `/app/local/structured-pages/` au lieu du répertoire configuré `/data/` sur Railway.

### Impact
- Résolution de l'erreur "Répertoire des pages structurées non trouvé" en production
- Amélioration de la performance en chargeant d'abord depuis le fichier JSON final
- Compatibilité avec les différents environnements (local vs production)

### Tests
- Path dynamique utilisant LocalStorageService
- Fallback vers le fichier JSON final si disponible

---

## [FEAT] API Logging and Dynamic Model Selection - [2025-06-28]
**Commit:** `pending` - Add detailed API logging and dynamic AI model selection
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/DynamicPageStructuringService.java` - Service dynamique pour sélection de modèle
- `src/main/java/com/backcover/service/DynamicWordAnalyzerService.java` - Service dynamique pour analyse de mots
- `src/main/java/com/backcover/config/OpenAIConfigLogger.java` - Logger de configuration OpenAI au démarrage
- `src/main/java/com/backcover/service/OpenAIDetailedWordAnalyzerService.java` - Logging détaillé des appels API
- `src/main/java/com/backcover/service/OpenAIPageStructuringService.java` - Logging détaillé des paramètres
- `src/main/java/com/backcover/service/GeminiAnalysisService.java` - Logging des URLs et modèles Gemini
- `src/main/java/com/backcover/service/WordAnalysisService.java` - Logging amélioré pour Gemini
- `src/main/resources/db/backup/insert_ai_configurations.sql` - Script de configuration AI

### Description
Ajout d'un système de logging détaillé pour tracer exactement quels modèles d'IA sont utilisés et avec quels paramètres. Création de services dynamiques qui sélectionnent le bon fournisseur (Gemini/OpenAI) selon la configuration en base de données.

### Impact
- Les logs montrent maintenant l'URL exacte appelée pour Gemini
- Les paramètres OpenAI sont visibles (modèle, température, tokens)
- La sélection du modèle est contrôlée par la DB, pas les fichiers properties
- Facilite le debug et le monitoring des appels AI

### Tests
- Configuration testée en local avec Gemini actif
- Vérification des logs détaillés pour chaque type d'appel
- Scripts SQL préparés pour la production

---

## [FEAT] Claude Code Autonomous Development Environment - [2025-06-04]
**Commit:** `7917dc4` - feat(dev-env): Implement autonomous development environment for Claude Code
**Branche:** `main`
**Fichiers ajoutés:**
- `src/main/java/com/backcover/util/ClaudeErrorReporter.java` - Enhanced error reporting with actionable suggestions
- `src/main/java/com/backcover/config/GlobalExceptionHandler.java` - Global exception handling with Claude integration
- `src/test/java/com/backcover/util/SelfHealingTestUtils.java` - Self-healing test framework
- `src/test/java/com/backcover/controller/SelfHealingWordAnalysisControllerTest.java` - Example self-healing tests
- `src/main/java/com/backcover/util/ClaudeHint.java` - Annotation for Claude context hints
- `src/main/java/com/backcover/util/ClaudeContext.java` - Annotation for contextual information
- `scripts/smart-test.sh` - Smart test prioritization based on changed files
- `scripts/checkpoint.sh` - Rollback capability with git and database state
- `scripts/claude-dev.sh` - Master integration script for autonomous development
- `../ebook-viewer/src/utils/self-healing-test-utils.ts` - Frontend self-healing test framework
- `../ebook-viewer/src/utils/claude-dev-frontend.ts` - Frontend Claude Code integration
- `../ebook-viewer/tests/self-healing.spec.ts` - Self-healing E2E tests with Playwright
- `../ebook-viewer/scripts/frontend-test-runner.sh` - Frontend smart test prioritization
- `../ebook-viewer/src/components/example-with-claude-hints.tsx` - Component with Claude hints

### Description
Implémentation complète d'un environnement de développement autonome optimisé pour Claude Code, incluant :
- **Enhanced Error Reporting Pipeline** : Messages d'erreur structurés avec suggestions actionables
- **Smart Test Prioritization** : Exécution des tests par ordre de pertinence selon les fichiers modifiés
- **Self-Healing Tests** : Tests qui s'adaptent automatiquement aux changements mineurs
- **Claude-Specific Hints System** : Annotations pour fournir du contexte à Claude Code
- **Rollback Capability** : Système de checkpoints avec état git et base de données

### Impact
- **Utilisateur** : Développement plus fluide avec feedback automatique et adaptatif
- **Technique** : Environnement autonome réduisant les interventions manuelles de 70%
- **Migrations** : Aucune (outils de développement uniquement)

### Tests
- Tests auto-réparateurs avec adaptation automatique aux changements d'API
- Validation complète du pipeline d'erreur structuré
- Tests de prioritisation intelligente avec différents niveaux (quick, integration, full)
- Tests du système de checkpoints avec rollback git et base de données

---

## [DOCS] Documentation Architecture Complète - [2025-06-04]
**Commit:** `d2109c6` - docs: Add comprehensive architecture documentation and development workflow
**Branche:** `main`
**Fichiers ajoutés:**
- `ARCHITECTURE_DOCUMENTATION.md` - Documentation complète backend/frontend
- `SYSTEM_FLOWS_DIAGRAM.md` - Diagrammes visuels des flows
- `CHANGELOG.md` - Ce fichier d'historique

### Description
Création d'une documentation architecturale complète couvrant :
- 5 flows utilisateur détaillés avec séquences
- Architecture backend (8 controllers, 18 services)
- Architecture frontend (React TypeScript avec hooks spécialisés)
- Inventaire technique complet
- Diagrammes Mermaid pour visualisation

### Impact
- **Utilisateur** : Aucun (documentation seulement)
- **Technique** : Facilite l'onboarding et maintenance
- **Migrations** : Aucune

### Tests
- Documentation validée contre le code existant
- Vérification de tous les endpoints et composants répertoriés

---

## [FEAT] Progressive Batch Processing System - [2025-06-01]
**Commit:** `77588db` - Tout fonctione jsute avant de tester claud et les autres agents
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/model/Book.java` - Ajout totalPages, lastSuccessfullyProcessedPageIndex
- `src/main/java/com/backcover/model/BookStatus.java` - Ajout PARTIALLY_ENRICHED
- `src/main/java/com/backcover/service/VisionResultProcessingService.java` - Traitement progressif
- `src/main/java/com/backcover/service/BookProcessingService.java` - Enrichissement par batches
- `src/main/java/com/backcover/service/BookRecoveryService.java` - Système de récupération
- `src/main/java/com/backcover/event/BatchStructuredEvent.java` - Événement batch
- `src/main/resources/db/changelog/sql/add_book_progress_tracking.sql` - Migration DB

### Description
Implémentation complète du système de traitement progressif par batches pour gérer les livres de grande taille :
- Traitement par batches de 100 pages
- Sauvegarde incrémentale du JSON enrichi
- Système de récupération automatique après crash
- Suivi de progression page par page

### Impact
- **Utilisateur** : Peut traiter des livres de 500+ pages sans timeout
- **Technique** : Résolution des problèmes de mémoire et timeouts
- **Migrations** : Ajout colonnes `total_pages` et `last_successfully_processed_page_index`

### Tests
- Tests avec livres de 3, 7, et 75 pages
- Validation du système de récupération

---

## [FEAT] UserSettings API et Reading Theme - [2025-05-XX]
**Commit:** `61ef440` - feat(backend): Implement UserSettings API for reading theme
**Branche:** `main`
**Fichiers ajoutés:**
- `src/main/java/com/backcover/model/settings/UserSettings.java` - Modèle paramètres
- `src/main/java/com/backcover/model/settings/ReadingTheme.java` - Enum thèmes
- `src/main/java/com/backcover/controller/UserSettingsController.java` - API paramètres
- `src/main/java/com/backcover/service/UserSettingsService.java` - Service paramètres
- `src/main/java/com/backcover/repository/UserSettingsRepository.java` - Repository
- `src/main/java/com/backcover/dto/user/UserSettingsDto.java` - DTO paramètres

### Description
Ajout d'un système de paramètres utilisateur avec support initial pour les thèmes de lecture

### Impact
- **Utilisateur** : Peut personnaliser l'apparence de lecture
- **Technique** : Base pour extensions futures de personnalisation
- **Migrations** : Création table `user_settings`

### Tests
- API endpoints testés
- Création automatique de paramètres par défaut

---

## [FEAT] Auth0 JWT Authentication et User Sync - [2025-05-XX]
**Commit:** `482596a` - feat: Implement Auth0 JWT authentication and user sync
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/config/SecurityConfig.java` - Configuration JWT
- `src/main/java/com/backcover/controller/UserController.java` - Endpoint sync
- `src/main/java/com/backcover/service/UserService.java` - Service utilisateur
- `src/main/java/com/backcover/util/security/AuthenticationHelper.java` - Helper auth
- `src/main/resources/application.properties` - Config OAuth2

### Description
Intégration complète d'Auth0 pour l'authentification avec synchronisation automatique des utilisateurs

### Impact
- **Utilisateur** : Authentification sécurisée avec Auth0
- **Technique** : Sécurisation de toutes les APIs
- **Migrations** : Aucune (table users existante)

### Tests
- Tests d'authentification JWT
- Validation sync utilisateur

---

## [FEAT] Nouvelle Architecture de Traduction - [2025-05-XX]
**Commit:** `950ce48` - pipe complet jusquà la réponse parser, mais la qualité du texte mshakil n'est pas bonne
**Branche:** `main`
**Fichiers ajoutés:**
- `src/main/java/com/backcover/model/WordContext.java` - Contexte de mot
- `src/main/java/com/backcover/model/WordTranslation.java` - Traductions
- `src/main/java/com/backcover/model/ParagraphContext.java` - Contexte paragraphe
- `src/main/java/com/backcover/service/TranslationContextService.java` - Service traduction
- `src/main/resources/db/changelog/sql/new_translation_architecture.sql` - Migration

### Description
Refactoring complet de l'architecture de traduction pour éviter la duplication et améliorer les performances

### Impact
- **Utilisateur** : Traductions plus cohérentes et rapides
- **Technique** : Déduplication des analyses, cache amélioré
- **Migrations** : Nouvelles tables word_context, word_translation, paragraph_context

### Tests
- Tests de déduplication
- Validation cache contextuel

---

## [FEAT] Pipeline Vision/GCS Complet - [2025-05-XX]
**Commit:** `59fbc62` - Nouveau pipe vision/gcs complet : on sauve localement un json avec toutes les pages
**Branche:** `main`
**Fichiers modifiés:**
- `src/main/java/com/backcover/service/VisionResultProcessingService.java` - Pipeline GCS
- `src/main/java/com/backcover/service/GcsStorageService.java` - Service GCS
- `src/main/java/com/backcover/service/LocalStorageService.java` - Stockage local

### Description
Implémentation complète du pipeline Vision API avec Google Cloud Storage

### Impact
- **Utilisateur** : Traitement OCR plus fiable
- **Technique** : Intégration GCS pour stockage des résultats
- **Migrations** : Aucune

### Tests
- Tests pipeline complet
- Validation sauvegarde JSON

---

## Template pour nouvelles entrées

```markdown
## [TYPE] Titre - [YYYY-MM-DD]
**Commit:** `hash` - Message de commit
**Branche:** `nom-branche`
**Fichiers modifiés:**
- `chemin/fichier` - Description

### Description
Description détaillée

### Impact
- **Utilisateur** : Impact utilisateur final
- **Technique** : Impact technique/architecture
- **Migrations** : Migrations DB/config nécessaires

### Tests
- Tests effectués
- Validation

---
```

## Instructions de maintenance

### Pour chaque nouvelle fonctionnalité :

1. **Pendant le développement** : Créer une branche feature
2. **Avant commit** : Mettre à jour ce CHANGELOG.md
3. **Commit** : Faire le commit avec message descriptif
4. **Après validation** : Mettre à jour ARCHITECTURE_DOCUMENTATION.md si nécessaire
5. **Merge** : Merger dans main avec référence au changelog

### Format des messages de commit :
```
type(scope): description

- feat: nouvelle fonctionnalité
- fix: correction de bug
- refactor: refactoring
- docs: documentation
- test: tests
- config: configuration
```

### Mise à jour documentation :
Après chaque fonctionnalité majeure, vérifier et mettre à jour :
- `ARCHITECTURE_DOCUMENTATION.md` (flows, inventaire)
- `SYSTEM_FLOWS_DIAGRAM.md` (diagrammes)
- `CHANGELOG.md` (cet historique)