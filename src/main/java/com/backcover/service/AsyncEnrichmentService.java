package com.backcover.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backcover.model.Book;
import com.backcover.model.BookMetadataTranslation;
import com.backcover.model.BookStatus;
import com.backcover.repository.BookMetadataTranslationRepository;
import com.backcover.repository.BookRepository;
import com.backcover.service.PdfMetadataService.LocalizedMetadata;
import com.backcover.service.PdfMetadataService.MultiLangMetadata;
import com.backcover.service.gemini.EnrichmentResultPersister;
import com.backcover.service.gemini.GeminiPdfEnrichmentService;
import com.backcover.service.gemini.GeminiPdfEnrichmentService.BookContext;
import com.backcover.service.gemini.GeminiPdfEnrichmentService.EnrichmentResult;
import com.backcover.service.storage.R2StorageService;

/**
 * Service for asynchronous book enrichment.
 * Allows the upload endpoint to return immediately while enrichment happens in background.
 * Supports resumable chunked processing for large PDFs.
 */
@Service
public class AsyncEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(AsyncEnrichmentService.class);

    private final BookRepository bookRepository;
    private final BookMetadataTranslationRepository translationRepository;
    private final GeminiPdfEnrichmentService geminiEnrichmentService;
    private final EnrichmentResultPersister resultPersister;
    private final PdfMetadataService pdfMetadataService;
    private final R2StorageService r2StorageService;

    public AsyncEnrichmentService(
            BookRepository bookRepository,
            BookMetadataTranslationRepository translationRepository,
            GeminiPdfEnrichmentService geminiEnrichmentService,
            EnrichmentResultPersister resultPersister,
            PdfMetadataService pdfMetadataService,
            R2StorageService r2StorageService) {
        this.bookRepository = bookRepository;
        this.translationRepository = translationRepository;
        this.geminiEnrichmentService = geminiEnrichmentService;
        this.resultPersister = resultPersister;
        this.pdfMetadataService = pdfMetadataService;
        this.r2StorageService = r2StorageService;
    }

    /**
     * Enriches a book asynchronously using Gemini API directly.
     * This method returns immediately and processes the book in a separate thread.
     * Supports resumption from partial completion (PARTIALLY_ENRICHED status).
     *
     * @param bookId The UUID of the book to enrich
     */
    @Async
    @Transactional
    public void enrichBookAsync(UUID bookId) {
        log.info("[ASYNC-ENRICH] Starting async enrichment for book: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("[ASYNC-ENRICH] Book not found: " + bookId));

        // Check if this is a resume
        int resumeFromPage = book.getLastSuccessfullyProcessedPageIndex();
        boolean isResume = resumeFromPage >= 0;

        if (isResume) {
            log.info("[ASYNC-ENRICH] Resuming from page {} for book: {}", resumeFromPage, bookId);
        }

        // Update status to PROCESSING
        book.setStatus(BookStatus.ENRICHMENT_IN_PROGRESS);
        book.setProcessingDetails(isResume
            ? "Reprise de l'enrichissement depuis la page " + (resumeFromPage + 1) + "..."
            : "Extraction des métadonnées multi-langues...");
        bookRepository.saveAndFlush(book);

        try {
            // Only extract metadata on fresh start (not resume)
            if (!isResume) {
                extractAndPersistMetadata(book);
            }

            // Reload book to get latest state after metadata extraction
            book = bookRepository.findById(bookId).orElseThrow();

            // Build context from book metadata for better tashkeel quality
            BookContext context = new BookContext(
                book.getTitle(),
                book.getAuthorName(),
                book.getGenre(),
                book.getDescription()
            );

            // Full enrichment with progress tracking
            book.setProcessingDetails("Enrichissement Gemini en cours...");
            bookRepository.saveAndFlush(book);

            EnrichmentResult result = geminiEnrichmentService.enrichBook(
                bookId.toString(),
                resumeFromPage,
                context,
                (lastProcessedPage, totalPages) -> updateProgress(bookId, lastProcessedPage, totalPages)
            );

            // Result already persisted by chunk processing, but persist final if needed
            String resultPath = resultPersister.mergeAndPersist(bookId.toString(), result.pages());

            // Reload book to avoid stale state
            book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                log.error("[ASYNC-ENRICH] Book disappeared during enrichment: {}", bookId);
                return;
            }

            book.setStatus(BookStatus.COMPLETED);
            book.setTotalPages(result.pages().size());
            book.setProcessingDetails("Enrichissement terminé");
            book.setFinalContentPath(resultPath);
            log.info("[ASYNC-ENRICH] Enrichment completed for book: {} ({} pages, {} tokens)",
                    bookId, result.pages().size(), result.totalTokens());

        } catch (Exception e) {
            log.error("[ASYNC-ENRICH] Enrichment error for book {}: {}", bookId, e.getMessage(), e);

            // Reload book to update status
            book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                // If some progress was made, mark as PARTIALLY_ENRICHED (can be resumed)
                // Otherwise mark as FAILED
                if (book.getLastSuccessfullyProcessedPageIndex() >= 0) {
                    book.setStatus(BookStatus.PARTIALLY_ENRICHED);
                    book.setProcessingDetails("Erreur chunk (reprendra automatiquement): " + e.getMessage());
                    log.info("[ASYNC-ENRICH] Marked as PARTIALLY_ENRICHED (can resume from page {})",
                        book.getLastSuccessfullyProcessedPageIndex());
                } else {
                    book.setStatus(BookStatus.FAILED);
                    book.setProcessingDetails("Enrichissement error: " + e.getMessage());
                }
            }
        }

        if (book != null) {
            bookRepository.saveAndFlush(book);
            log.info("[ASYNC-ENRICH] Final status for book {}: {}", bookId, book.getStatus());
        }
    }

    /**
     * Extracts metadata from PDF and persists it to the book and translations table.
     */
    private void extractAndPersistMetadata(Book book) {
        UUID bookId = book.getId();

        // 1. Load PDF from R2
        String r2Key = "books/raw/" + bookId + ".pdf";
        byte[] pdfBytes = r2StorageService.downloadFile(r2Key)
            .orElseThrow(() -> new RuntimeException("PDF not found: " + r2Key));

        // 2. Extract multi-language metadata - NO FALLBACK, will throw if extraction fails
        MultiLangMetadata metadata = pdfMetadataService.extractMultiLangMetadata(pdfBytes);

        // 3. Validate Arabic metadata exists (required)
        LocalizedMetadata arMetadata = metadata.translations().get("ar");
        if (arMetadata == null || arMetadata.title() == null) {
            throw new RuntimeException("Metadata extraction failed: Arabic title is required but was not extracted");
        }

        // 4. Update Book with Arabic metadata (primary language)
        book.setTitle(arMetadata.title());
        if (arMetadata.genre() != null) {
            book.setGenre(arMetadata.genre());
        }
        if (arMetadata.description() != null) {
            book.setDescription(arMetadata.description());
        }
        if (metadata.author() != null) {
            book.setAuthorName(metadata.author());
        }
        if (metadata.publishedDate() != null) {
            book.setPublishedDateText(metadata.publishedDate());
        }
        book.setProcessingDetails("Sauvegarde des traductions...");
        bookRepository.saveAndFlush(book);
        log.info("[ASYNC-ENRICH] Metadata extracted: {} languages, author={}",
            metadata.translations().size(), metadata.author());

        // 5. Persist all translations
        persistTranslations(book, metadata.translations());
    }

    /**
     * Updates book progress during chunk processing.
     * Called after each chunk completes.
     */
    private void updateProgress(UUID bookId, int lastProcessedPage, int totalPages) {
        try {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                book.setLastSuccessfullyProcessedPageIndex(lastProcessedPage);
                book.setTotalPages(totalPages);
                int progress = (int) ((lastProcessedPage + 1) * 100.0 / totalPages);
                book.setProcessingDetails(String.format("Enrichissement: %d/%d pages (%d%%)",
                    lastProcessedPage + 1, totalPages, progress));
                bookRepository.saveAndFlush(book);
                log.debug("[ASYNC-ENRICH] Progress updated: page {}/{} for book {}",
                    lastProcessedPage + 1, totalPages, bookId);
            }
        } catch (Exception e) {
            log.warn("[ASYNC-ENRICH] Failed to update progress for book {}: {}", bookId, e.getMessage());
            // Don't fail the whole enrichment just because progress update failed
        }
    }

    private void persistTranslations(Book book, Map<String, LocalizedMetadata> translations) {
        int saved = 0;
        for (Map.Entry<String, LocalizedMetadata> entry : translations.entrySet()) {
            String langCode = entry.getKey();
            LocalizedMetadata localized = entry.getValue();

            BookMetadataTranslation translation = new BookMetadataTranslation(book, langCode);
            translation.setTitle(localized.title());
            translation.setGenre(localized.genre());
            translation.setDescription(localized.description());
            translation.setAuthor(localized.author());

            translationRepository.save(translation);
            saved++;
        }
        log.info("[ASYNC-ENRICH] Persisted {} translations for book {}", saved, book.getId());
    }
}
