package com.backcover.service.gemini;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.backcover.config.gemini.GeminiSyncConfig;
import com.backcover.dto.gemini.GeminiRequest;
import com.backcover.dto.gemini.GeminiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP client for Gemini API with retry and timeout support.
 */
@Slf4j
@Component
public class GeminiApiClient {

    private final GeminiSyncConfig config;
    private final RestClient restClient;

    public GeminiApiClient(GeminiSyncConfig config) {
        this.config = config;
        this.restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Sends a generateContent request to Gemini API.
     *
     * @param request The request payload
     * @return The Gemini response
     * @throws GeminiApiException if the request fails
     */
    public GeminiResponse generateContent(GeminiRequest request) {
        // Construct full URL: baseUrl + model + :generateContent?key=apiKey
        String url = config.getBaseUrl() + config.getModel() + ":generateContent?key=" + config.getApiKey();

        log.info("[GEMINI-SYNC] Sending request to model: {}", config.getModel());

        Exception lastException = null;

        for (int attempt = 1; attempt <= config.getMaxRetries(); attempt++) {
            try {
                log.debug("[GEMINI-SYNC] Attempt {}/{}", attempt, config.getMaxRetries());

                GeminiResponse response = restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body;
                        try {
                            body = new String(res.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "Unable to read error body";
                        }
                        log.error("[GEMINI-SYNC] API error: {} - {}", res.getStatusCode(), body);
                        throw new GeminiApiException(
                            "Gemini API error: " + res.getStatusCode() + " - " + body,
                            res.getStatusCode().value()
                        );
                    })
                    .body(GeminiResponse.class);

                if (response != null) {
                    logResponse(response);

                    if (response.isTruncated()) {
                        log.warn("[GEMINI-SYNC] Response was truncated (MAX_TOKENS)");
                    }

                    return response;
                }

            } catch (GeminiApiException e) {
                lastException = e;
                // Don't retry on client errors (4xx)
                if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                    throw e;
                }
                log.warn("[GEMINI-SYNC] Request failed, attempt {}/{}: {}",
                    attempt, config.getMaxRetries(), e.getMessage());

            } catch (Exception e) {
                lastException = e;
                log.warn("[GEMINI-SYNC] Request failed, attempt {}/{}: {}",
                    attempt, config.getMaxRetries(), e.getMessage());
            }

            // Wait before retry (exponential backoff)
            if (attempt < config.getMaxRetries()) {
                long delay = config.getRetryDelayMs() * (long) Math.pow(2, attempt - 1);
                log.debug("[GEMINI-SYNC] Waiting {}ms before retry", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new GeminiApiException("Request interrupted", ie);
                }
            }
        }

        throw new GeminiApiException(
            "Gemini API request failed after " + config.getMaxRetries() + " attempts",
            lastException
        );
    }

    private void logResponse(GeminiResponse response) {
        if (response.getUsageMetadata() != null) {
            log.info("[GEMINI-SYNC] Response received - Tokens: input={}, output={}, total={}",
                response.getUsageMetadata().getPromptTokenCount(),
                response.getUsageMetadata().getCandidatesTokenCount(),
                response.getUsageMetadata().getTotalTokenCount());
        }
        log.debug("[GEMINI-SYNC] Finish reason: {}", response.getFinishReason());
    }

    /**
     * Exception for Gemini API errors.
     */
    public static class GeminiApiException extends RuntimeException {
        private final int statusCode;

        public GeminiApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public GeminiApiException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = 0;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
