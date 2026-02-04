package com.backcover.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.backcover.model.Book;
import com.backcover.model.BookStatus;
import com.backcover.repository.BookRepository;

/**
 * Service for automatic recovery of interrupted book enrichments.
 * Runs on application startup and periodically to resume any books
 * that were left in ENRICHMENT_IN_PROGRESS or PARTIALLY_ENRICHED status.
 */
@Service
public class EnrichmentRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentRecoveryService.class);

    private final BookRepository bookRepository;
    private final AsyncEnrichmentService asyncEnrichmentService;

    @Value("${enrichment.recovery.enabled:true}")
    private boolean recoveryEnabled;

    public EnrichmentRecoveryService(
            BookRepository bookRepository,
            AsyncEnrichmentService asyncEnrichmentService) {
        this.bookRepository = bookRepository;
        this.asyncEnrichmentService = asyncEnrichmentService;
    }

    /**
     * Runs on application startup to recover any interrupted enrichments.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverOnStartup() {
        if (!recoveryEnabled) {
            log.info("[RECOVERY] Recovery is disabled");
            return;
        }
        log.info("[RECOVERY] Startup check for interrupted enrichments...");
        recoverInterruptedEnrichments();
    }

    /**
     * Runs periodically (default: every 30 minutes) to recover interrupted enrichments.
     * This handles cases where Gemini API was temporarily unavailable.
     */
    @Scheduled(fixedDelayString = "${enrichment.recovery.interval-ms:1800000}")
    public void recoverPeriodically() {
        if (!recoveryEnabled) {
            return;
        }
        log.debug("[RECOVERY] Periodic check for interrupted enrichments...");
        recoverInterruptedEnrichments();
    }

    /**
     * Finds and resumes all interrupted enrichments.
     */
    private void recoverInterruptedEnrichments() {
        List<Book> interrupted = bookRepository.findByStatusIn(List.of(
            BookStatus.ENRICHMENT_IN_PROGRESS,
            BookStatus.PARTIALLY_ENRICHED
        ));

        if (interrupted.isEmpty()) {
            log.debug("[RECOVERY] No interrupted enrichments found");
            return;
        }

        log.info("[RECOVERY] Found {} interrupted books, resuming...", interrupted.size());

        for (Book book : interrupted) {
            log.info("[RECOVERY] Resuming book {} '{}' from page {} (status: {})",
                book.getId(),
                book.getTitle(),
                book.getLastSuccessfullyProcessedPageIndex(),
                book.getStatus());

            try {
                asyncEnrichmentService.enrichBookAsync(book.getId());
            } catch (Exception e) {
                log.error("[RECOVERY] Failed to resume book {}: {}", book.getId(), e.getMessage());
            }
        }
    }
}
