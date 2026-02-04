package com.backcover.service.prompt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Stub implementation of PromptUsageTracker
 * Tracking functionality has been removed
 */
@Service
public class PromptUsageTracker {
    
    private static final Logger log = LoggerFactory.getLogger(PromptUsageTracker.class);
    
    /**
     * Stub method - no longer tracks usage
     */
    public void trackUsage(String promptIdentifier, String promptVersion, String llmModel,
                          String usageType, int inputTokens, int outputTokens,
                          long executionTimeMs, boolean success, Double estimatedCostUsd,
                          Map<String, Object> metadata) {
        // No-op - usage tracking removed
        log.debug("Usage tracking disabled for prompt: {}", promptIdentifier);
    }
    
    /**
     * Stub method - no longer tracks async usage
     */
    public CompletableFuture<Void> trackUsageAsync(String promptIdentifier, String promptVersion,
                                                   String llmModel, String usageType,
                                                   int inputTokens, int outputTokens,
                                                   long executionTimeMs, boolean success,
                                                   Double estimatedCostUsd, Map<String, Object> metadata) {
        // No-op - usage tracking removed
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Stub class for TrackingContext
     */
    public static class TrackingContext {
        // Empty stub class
    }
    
    /**
     * Stub method - returns empty tracking context
     */
    public TrackingContext startTracking(Object... params) {
        return new TrackingContext();
    }
    
    /**
     * Stub method - completes tracking
     */
    public void completeTracking(Object context, String response, int inputTokens, 
                                int outputTokens, double cost, int statusCode) {
        // No-op - tracking removed
    }
    
    /**
     * Stub method - completes tracking with error
     */
    public void completeTrackingWithError(Object context, String error, int statusCode) {
        // No-op - tracking removed
    }
    
    /**
     * Stub method - returns default usage type
     */
    public enum UsageType {
        WORD_ANALYSIS("WORD_ANALYSIS"),
        PAGE_STRUCTURING("PAGE_STRUCTURING"),
        BOOK_PROCESSING("BOOK_PROCESSING"),
        TRANSLATION("TRANSLATION"),
        OTHER("OTHER");
        
        private final String value;
        
        UsageType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}