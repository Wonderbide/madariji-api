package com.backcover.service.gemini;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.backcover.dto.gemini.StructuredBookResponse.StructuredPage;
import com.backcover.service.gemini.GeminiPdfEnrichmentService.EnrichmentResult;
import com.backcover.service.storage.R2StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * Persists enrichment results to R2 storage.
 */
@Slf4j
@Service
public class EnrichmentResultPersister {

    private final R2StorageService r2Storage;
    private final ObjectMapper objectMapper;

    public EnrichmentResultPersister(
            @Qualifier("mainR2Storage") R2StorageService r2Storage,
            ObjectMapper objectMapper) {
        this.r2Storage = r2Storage;
        this.objectMapper = objectMapper;
    }

    /**
     * Persists the enrichment result to R2.
     *
     * @param result The enrichment result to persist
     * @return The R2 key where the result was saved
     */
    public String persist(EnrichmentResult result) {
        String r2Key = "batch/structured/" + result.bookId() + "-structured.json";

        log.info("[PERSIST] Saving structured result to R2: {}", r2Key);

        try {
            // Convert pages to JSON with pretty printing
            ObjectMapper prettyMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);

            String jsonContent = prettyMapper.writeValueAsString(result.pages());

            // Upload to R2 with metadata
            Map<String, String> metadata = Map.of(
                "content-type", "application/json",
                "book-id", result.bookId(),
                "source", "sync-enrichment-v2",
                "processed-at", Instant.now().toString(),
                "page-count", String.valueOf(result.pages().size()),
                "duration-ms", String.valueOf(result.durationMs()),
                "total-tokens", String.valueOf(result.totalTokens()),
                "was-truncated", String.valueOf(result.wasTruncated())
            );

            r2Storage.uploadFile(r2Key, jsonContent.getBytes(), metadata);

            log.info("[PERSIST] Successfully saved {} pages to {}", result.pages().size(), r2Key);

            return r2Key;

        } catch (JsonProcessingException e) {
            log.error("[PERSIST] Failed to serialize result: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize enrichment result", e);
        }
    }

    /**
     * Merges new pages with existing results and persists to R2.
     * If no existing result, creates a new file.
     * Pages are sorted by page number after merge.
     *
     * @param bookId The book UUID
     * @param newPages The new pages to merge
     * @return The R2 key where the result was saved
     */
    public String mergeAndPersist(String bookId, List<StructuredPage> newPages) {
        String r2Key = "batch/structured/" + bookId + "-structured.json";

        log.info("[PERSIST] Merging {} new pages for book {}", newPages.size(), bookId);

        // Load existing pages (or empty list)
        List<StructuredPage> existingPages = load(bookId);
        if (existingPages == null) {
            existingPages = new ArrayList<>();
        }

        // Merge: new pages replace existing ones with same page number
        List<StructuredPage> merged = new ArrayList<>(existingPages);
        for (StructuredPage newPage : newPages) {
            merged.removeIf(p -> p.getPageNumber() == newPage.getPageNumber());
            merged.add(newPage);
        }

        // Sort by page number
        merged.sort(Comparator.comparingInt(StructuredPage::getPageNumber));

        try {
            ObjectMapper prettyMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);

            String jsonContent = prettyMapper.writeValueAsString(merged);

            Map<String, String> metadata = Map.of(
                "content-type", "application/json",
                "book-id", bookId,
                "source", "sync-enrichment-chunked",
                "processed-at", Instant.now().toString(),
                "page-count", String.valueOf(merged.size())
            );

            r2Storage.uploadFile(r2Key, jsonContent.getBytes(), metadata);

            log.info("[PERSIST] Merged and saved {} total pages to {}", merged.size(), r2Key);

            return r2Key;

        } catch (JsonProcessingException e) {
            log.error("[PERSIST] Failed to serialize merged result: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize merged enrichment result", e);
        }
    }

    /**
     * Checks if a structured result already exists for a book.
     *
     * @param bookId The book UUID
     * @return true if the result exists
     */
    public boolean exists(String bookId) {
        String r2Key = "batch/structured/" + bookId + "-structured.json";
        return r2Storage.fileExists(r2Key);
    }

    /**
     * Loads an existing structured result.
     *
     * @param bookId The book UUID
     * @return The list of structured pages, or null if not found
     */
    public List<StructuredPage> load(String bookId) {
        String r2Key = "batch/structured/" + bookId + "-structured.json";

        var contentOpt = r2Storage.downloadFile(r2Key);
        if (contentOpt.isEmpty()) {
            return null;
        }
        byte[] content = contentOpt.get();

        try {
            return objectMapper.readValue(
                content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, StructuredPage.class)
            );
        } catch (Exception e) {
            log.error("[PERSIST] Failed to load result for {}: {}", bookId, e.getMessage());
            return null;
        }
    }
}
