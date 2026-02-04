package com.backcover.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import com.backcover.dto.gemini.GeminiRequest;
import com.backcover.dto.gemini.GeminiResponse;
import com.backcover.service.gemini.GeminiApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfMetadataService {

    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;

    public static final List<String> SUPPORTED_LANGUAGES = List.of(
        "ar", "fr", "en", "es", "de", "it", "pt", "ru", "zh", "ja", "ms", "id", "hi", "ur"
    );

    // Existing record for backwards compatibility
    public record ExtractedMetadata(
        String title,
        String author,
        String genre,
        String description,
        String publishedDate
    ) {}

    // New records for multi-language support
    public record LocalizedMetadata(
        String title,
        String genre,
        String description,
        String author
    ) {}

    public record MultiLangMetadata(
        String author,
        String publishedDate,
        Map<String, LocalizedMetadata> translations
    ) {}

    // Existing method - kept for backwards compatibility
    public ExtractedMetadata extractMetadata(byte[] pdfBytes) {
        byte[] firstPages = extractFirstPages(pdfBytes, 3);
        log.info("[METADATA] Extracted first pages: {} KB", firstPages.length / 1024);

        GeminiRequest request = GeminiRequest.forPdfEnrichment(
            firstPages,
            getMetadataPrompt(),
            buildMetadataSchema(),
            0.2,
            4096
        );

        GeminiResponse response = geminiClient.generateContent(request);
        String jsonContent = response.getTextContent();
        log.info("[METADATA] Raw response: {}", jsonContent);

        return parseMetadataResponse(jsonContent);
    }

    // New method for multi-language metadata extraction
    public MultiLangMetadata extractMultiLangMetadata(byte[] pdfBytes) {
        byte[] firstPages = extractFirstPages(pdfBytes, 3);
        log.info("[METADATA-MULTILANG] Extracted first pages: {} KB", firstPages.length / 1024);

        GeminiRequest request = GeminiRequest.forPdfEnrichment(
            firstPages,
            getMultiLangMetadataPrompt(),
            buildMultiLangSchema(),
            0.3,    // slightly higher temperature for translations
            8192    // more tokens for 14 languages
        );

        GeminiResponse response = geminiClient.generateContent(request);
        String jsonContent = response.getTextContent();
        log.info("[METADATA-MULTILANG] Raw response length: {} chars",
            jsonContent != null ? jsonContent.length() : 0);

        return parseMultiLangResponse(jsonContent);
    }

    private byte[] extractFirstPages(byte[] pdf, int count) {
        try (PDDocument source = PDDocument.load(pdf);
             PDDocument target = new PDDocument()) {
            int pagesToExtract = Math.min(count, source.getNumberOfPages());
            for (int i = 0; i < pagesToExtract; i++) {
                target.addPage(source.getPage(i));
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            target.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("[METADATA] Failed to extract first pages", e);
            throw new RuntimeException("Failed to extract PDF pages", e);
        }
    }

    // Existing schema for backwards compatibility
    private Map<String, Object> buildMetadataSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "title", Map.of("type", "string"),
                "author", Map.of("type", "string"),
                "genre", Map.of("type", "string"),
                "description", Map.of("type", "string"),
                "publishedDate", Map.of("type", "string")
            ),
            "required", List.of("title")
        );
    }

    // New schema for multi-language metadata
    private Map<String, Object> buildMultiLangSchema() {
        Map<String, Object> localizedMetaSchema = Map.of(
            "type", "object",
            "properties", Map.of(
                "title", Map.of("type", "string"),
                "genre", Map.of("type", "string"),
                "description", Map.of("type", "string"),
                "author", Map.of("type", "string")
            )
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("author", Map.of("type", "string"));
        properties.put("publishedDate", Map.of("type", "string"));

        for (String lang : SUPPORTED_LANGUAGES) {
            properties.put(lang, localizedMetaSchema);
        }

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("ar")
        );
    }

    // Existing prompt for backwards compatibility
    private String getMetadataPrompt() {
        return """
            حلل الصفحات الأولى من هذا الكتاب واستخرج:
            - title: عنوان الكتاب (من الغلاف أو صفحة العنوان)
            - author: اسم المؤلف
            - genre: نوع الكتاب (شعر، رواية، مقالات، دراسات، تاريخ، إلخ)
            - description: جملة أو جملتان تصف محتوى الكتاب
            - publishedDate: سنة النشر إن وجدت

            أعد JSON فقط. إذا لم تتمكن من تحديد حقل، استخدم null.
            """;
    }

    // New prompt for multi-language metadata
    private String getMultiLangMetadataPrompt() {
        return """
            Analyze the first pages of this Arabic book and extract metadata.

            Return JSON with:
            - author: Author name (keep original Arabic script)
            - publishedDate: Publication year if found
            - For each of these 14 languages: ar, fr, en, es, de, it, pt, ru, zh, ja, ms, id, hi, ur
              - title: Book title translated
              - genre: Book genre/category translated
              - description: 1-2 sentences describing the book content translated
              - author: Author name transliterated to this language's script (e.g., Arabic محمد becomes "Muhammad" in English, "Mohammed" in French, "穆罕默德" in Chinese, "मुहम्मद" in Hindi)

            IMPORTANT:
            - The "ar" version should contain the original Arabic text from the book (including original Arabic author name)
            - For all other languages, provide accurate translations and transliterations
            - Use native script for each language (e.g., Chinese characters for zh, Devanagari for hi, Arabic script for ur)
            - If a field cannot be determined, use null
            """;
    }

    // Existing parser - NO FALLBACK
    private ExtractedMetadata parseMetadataResponse(String json) {
        if (json == null || json.isBlank()) {
            throw new RuntimeException("[METADATA] Empty response from Gemini - cannot extract metadata");
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String title = getTextOrNull(node, "title");
            if (title == null || title.isBlank()) {
                throw new RuntimeException("[METADATA] Title is required but was not extracted");
            }
            return new ExtractedMetadata(
                title,
                getTextOrNull(node, "author"),
                getTextOrNull(node, "genre"),
                getTextOrNull(node, "description"),
                getTextOrNull(node, "publishedDate")
            );
        } catch (RuntimeException e) {
            throw e; // Re-throw our own exceptions
        } catch (Exception e) {
            throw new RuntimeException("[METADATA] Failed to parse response: " + e.getMessage(), e);
        }
    }

    // New parser for multi-language metadata - NO FALLBACK
    private MultiLangMetadata parseMultiLangResponse(String json) {
        if (json == null || json.isBlank()) {
            throw new RuntimeException("[METADATA-MULTILANG] Empty response from Gemini - cannot extract metadata");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            String author = getTextOrNull(root, "author");
            String publishedDate = getTextOrNull(root, "publishedDate");

            Map<String, LocalizedMetadata> translations = new HashMap<>();
            for (String lang : SUPPORTED_LANGUAGES) {
                JsonNode langNode = root.get(lang);
                if (langNode != null && !langNode.isNull()) {
                    LocalizedMetadata localized = new LocalizedMetadata(
                        getTextOrNull(langNode, "title"),
                        getTextOrNull(langNode, "genre"),
                        getTextOrNull(langNode, "description"),
                        getTextOrNull(langNode, "author")
                    );
                    translations.put(lang, localized);
                }
            }

            // Validate that we got at least Arabic metadata
            if (!translations.containsKey("ar") || translations.get("ar").title() == null) {
                throw new RuntimeException("[METADATA-MULTILANG] Arabic metadata missing or incomplete - title is required");
            }

            log.info("[METADATA-MULTILANG] Parsed {} language translations", translations.size());
            return new MultiLangMetadata(author, publishedDate, translations);
        } catch (RuntimeException e) {
            throw e; // Re-throw our own exceptions
        } catch (Exception e) {
            throw new RuntimeException("[METADATA-MULTILANG] Failed to parse response: " + e.getMessage(), e);
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }
}
