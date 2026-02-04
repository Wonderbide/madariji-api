package com.backcover.service.gemini;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.backcover.config.gemini.GeminiSyncConfig;
import com.backcover.dto.gemini.GeminiRequest;
import com.backcover.dto.gemini.GeminiResponse;
import com.backcover.dto.gemini.StructuredBookResponse;
import com.backcover.dto.gemini.StructuredBookResponse.StructuredPage;
import com.backcover.service.storage.R2StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for synchronous PDF enrichment using Gemini API.
 * Supports chunked processing for large PDFs to avoid output token limits.
 */
@Slf4j
@Service
public class GeminiPdfEnrichmentService {

    private final GeminiSyncConfig config;
    private final GeminiApiClient geminiClient;
    private final R2StorageService r2Storage;
    private final BookStructureSchema schema;
    private final ObjectMapper objectMapper;
    private final EnrichmentResultPersister resultPersister;

    public GeminiPdfEnrichmentService(
            GeminiSyncConfig config,
            GeminiApiClient geminiClient,
            @Qualifier("mainR2Storage") R2StorageService r2Storage,
            BookStructureSchema schema,
            ObjectMapper objectMapper,
            EnrichmentResultPersister resultPersister) {
        this.config = config;
        this.geminiClient = geminiClient;
        this.r2Storage = r2Storage;
        this.schema = schema;
        this.objectMapper = objectMapper;
        this.resultPersister = resultPersister;
    }

    /**
     * Book context for enrichment (helps Gemini with tashkeel quality).
     */
    public record BookContext(
        String title,
        String author,
        String genre,
        String description
    ) {}

    /**
     * Callback interface for progress updates during chunk processing.
     */
    @FunctionalInterface
    public interface ChunkProgressCallback {
        void onChunkCompleted(int lastProcessedPageIndex, int totalPages);
    }

    /**
     * Enriches a book by sending its PDF directly to Gemini.
     * Backward compatible method - processes entire PDF in one call.
     *
     * @param bookId The book UUID
     * @return EnrichmentResult with structured pages and metadata
     */
    public EnrichmentResult enrichBook(String bookId) {
        return enrichBook(bookId, -1, null, null);
    }

    /**
     * Enriches a book with chunked processing support.
     *
     * @param bookId The book UUID
     * @param resumeFromPageIndex The last successfully processed page index (-1 for fresh start)
     * @param context Book context for better tashkeel quality (can be null)
     * @param progressCallback Callback for progress updates (can be null)
     * @return EnrichmentResult with structured pages and metadata
     */
    public EnrichmentResult enrichBook(String bookId, int resumeFromPageIndex, BookContext context, ChunkProgressCallback progressCallback) {
        log.info("[SYNC-ENRICH] Starting enrichment for book: {} (resume from page: {})", bookId, resumeFromPageIndex);
        long startTime = System.currentTimeMillis();
        int totalTokens = 0;

        // 1. Load PDF from R2
        String r2Key = "books/raw/" + bookId + ".pdf";
        log.info("[SYNC-ENRICH] Loading PDF from R2: {}", r2Key);

        byte[] pdfBytes = r2Storage.downloadFile(r2Key)
            .orElseThrow(() -> new EnrichmentException("PDF not found in R2: " + r2Key));

        if (pdfBytes.length == 0) {
            throw new EnrichmentException("PDF is empty: " + r2Key);
        }

        validatePdfSize(pdfBytes);
        log.info("[SYNC-ENRICH] PDF loaded: {} KB", pdfBytes.length / 1024);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            int totalPages = document.getNumberOfPages();
            int chunkSize = config.getChunkSize();

            log.info("[SYNC-ENRICH] PDF has {} pages, chunk size: {}", totalPages, chunkSize);

            // Load existing pages if resuming
            List<StructuredPage> allPages = new ArrayList<>();
            if (resumeFromPageIndex >= 0) {
                List<StructuredPage> existing = resultPersister.load(bookId);
                if (existing != null) {
                    allPages.addAll(existing);
                    log.info("[SYNC-ENRICH] Loaded {} existing pages from previous run", existing.size());
                }
            }

            // Calculate starting chunk
            int startChunkIndex = (resumeFromPageIndex + 1) / chunkSize;
            int totalChunks = (int) Math.ceil((double) totalPages / chunkSize);

            log.info("[SYNC-ENRICH] Processing chunks {} to {} (total: {})", startChunkIndex + 1, totalChunks, totalChunks);

            // Process each chunk
            for (int chunkIndex = startChunkIndex; chunkIndex < totalChunks; chunkIndex++) {
                int startPage = chunkIndex * chunkSize;
                int endPage = Math.min(startPage + chunkSize, totalPages);

                log.info("[CHUNK {}/{}] Processing pages {}-{}", chunkIndex + 1, totalChunks, startPage + 1, endPage);

                // Extract chunk PDF
                byte[] chunkPdf = extractPageRange(document, startPage, endPage);
                log.info("[CHUNK {}/{}] Extracted chunk PDF: {} KB", chunkIndex + 1, totalChunks, chunkPdf.length / 1024);

                // Build Gemini request with context
                String prompt = context != null
                    ? schema.getEnrichmentPrompt(context.title(), context.author(), context.genre(), context.description())
                    : schema.getEnrichmentPrompt();

                GeminiRequest request = GeminiRequest.forPdfEnrichment(
                    chunkPdf,
                    prompt,
                    schema.getSchema(),
                    config.getTemperature(),
                    config.getMaxOutputTokens()
                );

                // Call Gemini API
                log.info("[CHUNK {}/{}] Sending to Gemini API...", chunkIndex + 1, totalChunks);
                GeminiResponse response = geminiClient.generateContent(request);

                if (response.isTruncated()) {
                    log.warn("[CHUNK {}/{}] Response was truncated! Chunk may be too large.", chunkIndex + 1, totalChunks);
                }

                // Parse response
                String jsonContent = response.getTextContent();
                StructuredBookResponse structuredResponse = parseStructuredResponse(jsonContent);
                List<StructuredPage> chunkPages = structuredResponse.toStructuredFormat();

                // Adjust page numbers (Gemini returns 1-based for the chunk)
                adjustPageNumbers(chunkPages, startPage);

                // Add to all pages
                allPages.addAll(chunkPages);

                // Track tokens
                if (response.getUsageMetadata() != null) {
                    totalTokens += response.getUsageMetadata().getTotalTokenCount();
                }

                // Persist immediately for resilience
                resultPersister.mergeAndPersist(bookId, allPages);

                int lastProcessedPage = endPage - 1; // 0-based index
                log.info("[CHUNK {}/{}] Completed. Total pages so far: {}", chunkIndex + 1, totalChunks, allPages.size());

                // Notify progress
                if (progressCallback != null) {
                    progressCallback.onChunkCompleted(lastProcessedPage, totalPages);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[SYNC-ENRICH] Enrichment completed in {}ms. Total pages: {}, Total tokens: {}",
                duration, allPages.size(), totalTokens);

            return new EnrichmentResult(
                bookId,
                allPages,
                duration,
                totalTokens,
                false
            );

        } catch (IOException e) {
            throw new EnrichmentException("Failed to process PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a range of pages from a PDF document.
     *
     * @param document The source PDF document
     * @param startPage Start page index (0-based, inclusive)
     * @param endPage End page index (0-based, exclusive)
     * @return The extracted pages as a PDF byte array
     */
    private byte[] extractPageRange(PDDocument document, int startPage, int endPage) {
        try (PDDocument chunkDoc = new PDDocument()) {
            for (int i = startPage; i < endPage; i++) {
                chunkDoc.addPage(document.getPage(i));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            chunkDoc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new EnrichmentException("Failed to extract page range: " + e.getMessage(), e);
        }
    }

    /**
     * Adjusts page numbers in the chunk result to match the original PDF.
     * Gemini returns pages numbered 1 to N for each chunk, but we need
     * the original page numbers.
     *
     * @param pages The pages from the chunk
     * @param startPage The starting page index in the original PDF (0-based)
     */
    private void adjustPageNumbers(List<StructuredPage> pages, int startPage) {
        for (int i = 0; i < pages.size(); i++) {
            StructuredPage page = pages.get(i);
            // Gemini returns pageNumber starting at 1 for each chunk
            // We need to adjust to the actual page number in the original PDF
            int expectedPageInChunk = i + 1;
            int actualPageNumber = startPage + expectedPageInChunk;

            // Update page number if needed
            if (page.getPageNumber() != actualPageNumber) {
                page.setPageNumber(actualPageNumber);
            }
        }
    }

    private void validatePdfSize(byte[] pdfBytes) {
        int sizeMb = pdfBytes.length / (1024 * 1024);
        if (sizeMb > config.getMaxPdfSizeMb()) {
            throw new EnrichmentException(String.format(
                "PDF too large: %d MB (max: %d MB)",
                sizeMb,
                config.getMaxPdfSizeMb()
            ));
        }
    }

    private StructuredBookResponse parseStructuredResponse(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            throw new EnrichmentException("Empty response from LLM");
        }

        try {
            return objectMapper.readValue(jsonContent, StructuredBookResponse.class);
        } catch (JsonProcessingException e) {
            log.error("[SYNC-ENRICH] Failed to parse LLM response: {}", e.getMessage());
            log.debug("[SYNC-ENRICH] Raw response: {}", jsonContent);
            throw new EnrichmentException("Failed to parse structured response", e);
        }
    }

    /**
     * Result of an enrichment operation.
     */
    public record EnrichmentResult(
        String bookId,
        List<StructuredPage> pages,
        long durationMs,
        int totalTokens,
        boolean wasTruncated
    ) {}

    /**
     * Exception for enrichment errors.
     */
    public static class EnrichmentException extends RuntimeException {
        public EnrichmentException(String message) {
            super(message);
        }

        public EnrichmentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
