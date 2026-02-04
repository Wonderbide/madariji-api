package com.backcover.dto.gemini;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for Gemini API generateContent endpoint.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiRequest {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Data
    @Builder
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Part {
        private String text;
        private InlineData inlineData;

        public static Part text(String text) {
            return Part.builder().text(text).build();
        }

        public static Part pdf(byte[] pdfBytes) {
            return Part.builder()
                .inlineData(InlineData.builder()
                    .mimeType("application/pdf")
                    .data(Base64.getEncoder().encodeToString(pdfBytes))
                    .build())
                .build();
        }
    }

    @Data
    @Builder
    public static class InlineData {
        private String mimeType;
        private String data;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerationConfig {
        private String responseMimeType;
        private Map<String, Object> responseSchema;
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;
    }

    /**
     * Creates a request for PDF enrichment with structured JSON output.
     */
    public static GeminiRequest forPdfEnrichment(
            byte[] pdfBytes,
            String prompt,
            Map<String, Object> responseSchema,
            double temperature,
            int maxOutputTokens) {

        Content content = Content.builder()
            .role("user")
            .parts(List.of(
                Part.pdf(pdfBytes),
                Part.text(prompt)
            ))
            .build();

        GenerationConfig config = GenerationConfig.builder()
            .responseMimeType("application/json")
            .responseSchema(responseSchema)
            .temperature(temperature)
            .maxOutputTokens(maxOutputTokens)
            .topP(0.95)
            .topK(40)
            .build();

        return GeminiRequest.builder()
            .contents(List.of(content))
            .generationConfig(config)
            .build();
    }
}
