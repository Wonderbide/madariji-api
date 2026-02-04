package com.backcover.config.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for the synchronous Gemini PDF enrichment service.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "enrichment.sync")
public class GeminiSyncConfig {

    /**
     * Gemini API key (from environment)
     */
    private String apiKey;

    /**
     * Gemini model to use (e.g., gemini-2.0-flash, gemini-1.5-pro)
     */
    private String model = "gemini-2.0-flash";

    /**
     * API base URL
     */
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";

    /**
     * Maximum PDF size in MB
     */
    private int maxPdfSizeMb = 50;

    /**
     * Maximum pages per PDF
     */
    private int maxPages = 1000;

    /**
     * Request timeout in seconds
     */
    private int timeoutSeconds = 300;

    /**
     * Generation temperature (0.0 - 1.0)
     */
    private double temperature = 0.2;

    /**
     * Maximum output tokens
     */
    private int maxOutputTokens = 65536;

    /**
     * Number of retry attempts
     */
    private int maxRetries = 3;

    /**
     * Initial retry delay in milliseconds
     */
    private long retryDelayMs = 1000;

    /**
     * Number of pages per chunk for processing large PDFs.
     * Each chunk is sent to Gemini separately to avoid output token limits.
     */
    private int chunkSize = 50;
}
