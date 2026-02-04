# Backcover Development Guide

## Build Commands
- Build: `./mvnw clean install`
- Run: `./mvnw spring-boot:run`
- Run tests: `./mvnw test`
- Run a single test: `./mvnw test -Dtest=BackcoverApplicationTests#contextLoads`
- Lint/Format: No explicit lint command (using IDE features)

## Code Style Guidelines
- **Architecture**: Spring Boot with MVC pattern (controllers, services, repositories)
- **Classes**: Use Spring annotations (@RestController, @Service, @Repository, @Component)
- **Dependencies**: Constructor injection preferred over @Autowired
- **DTOs**: Use plain Java or records, Lombok is used selectively
- **Error Handling**: Use try/catch with specific exceptions, log errors properly
- **Logging**: Use SLF4J Logger with appropriate levels (info, warn, error)
- **Naming**: CamelCase for methods and variables, PascalCase for classes
- **Authentication**: Use Auth0 JWT tokens, AuthenticationHelper for security checks

## Development Tools
- Use Doppler for environment configuration management
- On a pas de remote repository

## CRITICAL REQUIREMENTS - MUST READ

### NO FALLBACK POLICY
**IMPORTANT**: L'utilisateur exige qu'il n'y ait JAMAIS de mécanisme de fallback dans l'application. 
- Si un service échoue, l'application doit échouer proprement avec un message d'erreur clair
- Ne jamais implémenter de logique "si X échoue, utiliser Y à la place"
- Cette règle s'applique particulièrement aux services d'IA (Gemini, OpenAI, etc.)
- Toujours propager les erreurs vers le haut plutôt que de masquer les problèmes avec des fallbacks

## PROGRESSIVE BATCH PROCESSING IMPLEMENTATION

### Implementation Status: ✅ COMPLETED
All phases of the progressive batch processing system have been implemented:

**Phase 1: Foundations** ✅
- Added `totalPages` and `lastSuccessfullyProcessedPageIndex` to Book model
- Added `PARTIALLY_ENRICHED` status to BookStatus enum
- Created `BookProgressDto` with automatic percentage calculation
- Added progress tracking endpoint: `GET /api/books/{bookId}/progress`
- Database migration created: `add_book_progress_tracking.sql`

**Phase 2: VisionResultProcessingService Refactoring** ✅
- Created `BatchStructuredEvent` for batch-by-batch processing
- Refactored `handleOcrCompletedAndProcessResults()` to process Vision results progressively
- Split processing into two phases: first extract all pages, then publish batch events
- Eliminated memory issues by avoiding loading all pages at once
- Added proper batch indexing and progress tracking

**Phase 3: BookProcessingService Batch Processing** ✅
- Added `BatchStructuredEvent` listener: `handleBatchStructured()`
- Implemented `enrichBatchWithStructuredData()` method for progressive enrichment
- Added crash recovery logic: skips already processed batches
- Implements incremental JSON file updates (load existing, merge new pages, save)
- Proper status transitions: AWAITING_ENRICHMENT → ENRICHMENT_IN_PROGRESS → PARTIALLY_ENRICHED → COMPLETED

**Phase 4: Crash Recovery System** ✅
- Created `BookRecoveryService` with automatic recovery capabilities
- Added `BookRecoveryController` with endpoints:
  - `POST /api/books/{bookId}/recover` - Recover specific book
  - `GET /api/books/{bookId}/recovery-status` - Check recovery status
  - `POST /api/books/recover-all` - Recover all partially processed books
- Created `ScheduledRecoveryService` for automatic recovery on startup and every 30 minutes
- Recovery system calculates remaining batches and republishes events

**Phase 5: Architecture Overview** ✅
The system now processes books as follows:
1. **OCR Processing**: Vision API processes PDF in 100-page batches
2. **Batch Events**: Each batch publishes a `BatchStructuredEvent`
3. **Progressive Enrichment**: Each batch is enriched independently and saved incrementally
4. **Progress Tracking**: `lastSuccessfullyProcessedPageIndex` tracks exact progress
5. **Crash Recovery**: Automatic detection and resumption from last successful page
6. **Mathematical Recovery**: `batchIndex = pageNumber / batchSize` for precise restart

### Key Benefits Achieved:
- ✅ **Memory Efficiency**: No more loading 500+ pages into memory
- ✅ **Crash Recovery**: Automatic resumption from exact failure point
- ✅ **Progress Tracking**: Real-time progress information via API
- ✅ **Scalability**: Handles books of any size (tested up to 500+ pages)
- ✅ **Reliability**: Transactional safety with incremental progress saves
- ✅ **User Experience**: Progress percentage and detailed status information

### Configuration:
- Batch size: 100 pages (configurable via `book.visionBatchSize`)
- Recovery schedule: Every 30 minutes + on application startup
- Progress tracking: Page-level precision with automatic percentage calculation

## CURRENT SPRINT STATUS

### Sprint 2: Prompt Management System ✅ COMPLETED
**Date**: December 16, 2025
**Status**: 🟢 COMPLETED (100% complete)

**Completed Features**:
1. ✅ Complete statistics system for prompts (costs, tokens, performance)
2. ✅ Model comparison functionality (Gemini vs GPT-4)
3. ✅ Temporal trends analysis (token usage, costs over time)
4. ✅ Quality metrics integration
5. ✅ RESTful APIs for prompt analytics
6. ✅ Quality reporting endpoints (QualityReportController)
   - POST `/api/quality-reports` - Submit quality reports
   - GET `/api/quality-reports` - List and filter reports
   - GET `/api/quality-reports/{id}` - Get specific report
   - PUT `/api/quality-reports/{id}` - Update report status
   - GET `/api/quality-reports/statistics` - Quality statistics
7. ✅ Dynamic prompt management (PromptTemplateController)
   - GET `/api/prompt-templates` - List templates with filtering
   - GET `/api/prompt-templates/{id}` - Get specific template
   - GET `/api/prompt-templates/by-identifier/{identifier}` - Get active version
   - GET `/api/prompt-templates/versions/{identifier}` - Get all versions
   - POST `/api/prompt-templates` - Create new template
   - PUT `/api/prompt-templates/{id}` - Update template
   - DELETE `/api/prompt-templates/{id}` - Delete template
   - POST `/api/prompt-templates/{id}/activate` - Activate version

**Next Sprints Overview**:
- **Sprint 3**: Dashboard APIs (overview, trends, optimization)
- **Sprint 4**: Prompt versioning system
- **Sprint 5**: A/B testing framework

**Architecture Notes**:
- Using Spring Data JPA with PostgreSQL
- Native queries for PostgreSQL-specific functions (PERCENTILE_CONT, DATE_TRUNC)
- Caching implemented for expensive statistics calculations
- DTOs used for all data transfer to optimize performance

**Important Files**:
- `/SPRINT_2_STATUS.md` - Detailed sprint status and architecture
- `com.backcover.service.prompt.PromptStatisticsService` - Core statistics service
- `com.backcover.controller.PromptDetailController` - Analytics endpoints
- add memory. don't try to guess what I want, stop congratulating me whatever the reason nor confirmeming. be cold and professional. don't systematicaly aligne with me, try to debate and discuss
- Plannifier tout changement en amont, confirmer le plan, construire le plan en baby test testable manuellement, ne jamais passer à l'étape suiivante avant confirmation par moi et si confirmation on fait un commit intermédiaire avant de coder la suite.
- on utilise doppler