package com.backcover.dto.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Response DTO from Gemini API generateContent endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;
    private String modelVersion;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
        private String finishReason;
        private List<SafetyRating> safetyRatings;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SafetyRating {
        private String category;
        private String probability;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }

    /**
     * Extracts the text content from the first candidate.
     */
    public String getTextContent() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }
        List<Part> parts = candidate.getContent().getParts();
        if (parts.isEmpty()) {
            return null;
        }
        return parts.get(0).getText();
    }

    /**
     * Checks if the response was truncated due to max tokens.
     */
    public boolean isTruncated() {
        if (candidates == null || candidates.isEmpty()) {
            return false;
        }
        return "MAX_TOKENS".equals(candidates.get(0).getFinishReason());
    }

    /**
     * Gets the finish reason for the first candidate.
     */
    public String getFinishReason() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0).getFinishReason();
    }
}
