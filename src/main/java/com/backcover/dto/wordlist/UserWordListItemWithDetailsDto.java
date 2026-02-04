package com.backcover.dto.wordlist;

import com.backcover.model.user.UserWordListItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class UserWordListItemWithDetailsDto {
    private static final Logger log = LoggerFactory.getLogger(UserWordListItemWithDetailsDto.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private UUID id;
    private UUID listId; // ID de la liste pour permettre suppression efficace
    private UUID bookId;
    private String bookTitle;
    private Integer pageNumber;
    private String wordInstanceId;
    private String wordText;
    private Instant addedAt;
    
    // Word analysis details
    private UUID wordAnalysisId;
    private WordAnalysisDetailsDto analysisDetails;
    private String analysisSource;

    // Constructors
    public UserWordListItemWithDetailsDto() {
    }

    public UserWordListItemWithDetailsDto(UUID id, UUID listId, UUID bookId, String bookTitle, Integer pageNumber, 
                                        String wordInstanceId, String wordText, Instant addedAt,
                                        UUID wordAnalysisId, WordAnalysisDetailsDto analysisDetails, String analysisSource) {
        this.id = id;
        this.listId = listId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordText = wordText;
        this.addedAt = addedAt;
        this.wordAnalysisId = wordAnalysisId;
        this.analysisDetails = analysisDetails;
        this.analysisSource = analysisSource;
    }

    // Factory method to create DTO from entity (default language: fr)
    public static UserWordListItemWithDetailsDto fromEntity(UserWordListItem entity) {
        return fromEntity(entity, "fr");
    }

    // Factory method to create DTO from entity with specific language
    public static UserWordListItemWithDetailsDto fromEntity(UserWordListItem entity, String languageCode) {
        if (entity == null) {
            return null;
        }

        UUID wordAnalysisId = null;
        WordAnalysisDetailsDto analysisDetails = null;
        String analysisSource = null;

        if (entity.getWordAnalysis() != null) {
            wordAnalysisId = entity.getWordAnalysis().getId();
            analysisSource = entity.getWordAnalysis().getSource();

            // Parse JSON to structured DTO
            String rawAnalysisData = entity.getWordAnalysis().getAnalysisData();
            if (rawAnalysisData != null && !rawAnalysisData.isBlank()) {
                try {
                    JsonNode jsonNode = OBJECT_MAPPER.readTree(rawAnalysisData);
                    analysisDetails = parseAnalysisData(jsonNode, languageCode);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse analysis data: {}", e.getMessage());
                }
            }
        }

        return new UserWordListItemWithDetailsDto(
            entity.getId(),
            entity.getWordList().getId(),
            entity.getBook().getId(),
            entity.getBook().getTitle(),
            entity.getPageNumber(),
            entity.getWordInstanceId(),
            entity.getWordText(),
            entity.getAddedAt(),
            wordAnalysisId,
            analysisDetails,
            analysisSource
        );
    }
    
    private static WordAnalysisDetailsDto parseAnalysisData(JsonNode jsonNode, String languageCode) {
        WordAnalysisDetailsDto dto = new WordAnalysisDetailsDto();

        dto.setType(jsonNode.path("type").asText(null));
        dto.setJidar(jsonNode.path("jidar").asText(null));
        dto.setRoot(jsonNode.path("root").asText(null));
        dto.setMasdar(jsonNode.path("masdar").asText(null));
        dto.setWazn(jsonNode.path("wazn").asText(null));
        dto.setCanonicalForm(jsonNode.path("canonical_form_from_llm").asText(null));
        dto.setTranslation(jsonNode.path("translation").asText(null));

        // Try details_by_language first (new format), then fall back to details (old format)
        JsonNode detailsNode = null;
        JsonNode detailsByLangNode = jsonNode.path("details_by_language");
        if (!detailsByLangNode.isMissingNode() && !detailsByLangNode.isNull()) {
            // Try requested language first, then fall back to "fr"
            detailsNode = detailsByLangNode.path(languageCode);
            if (detailsNode.isMissingNode() || detailsNode.isNull()) {
                detailsNode = detailsByLangNode.path("fr");
            }
        }
        // Fall back to simple "details" field if details_by_language not found
        if (detailsNode == null || detailsNode.isMissingNode() || detailsNode.isNull()) {
            detailsNode = jsonNode.path("details");
        }

        if (detailsNode != null && !detailsNode.isMissingNode() && !detailsNode.isNull()) {
            dto.setMeaning(detailsNode.path("meaning").asText(null));
            dto.setFunction(detailsNode.path("function").asText(null));
            // Store the full details object for additional data (gender, number, madi, mudari, amr, wazn, etc.)
            dto.setAdditionalDetails(detailsNode);
        }

        // Parse lexical_fields array
        JsonNode lexicalFieldsNode = jsonNode.path("lexical_fields");
        if (!lexicalFieldsNode.isMissingNode() && lexicalFieldsNode.isArray()) {
            java.util.List<String> lexicalFields = new java.util.ArrayList<>();
            for (JsonNode field : lexicalFieldsNode) {
                if (field.isTextual()) {
                    lexicalFields.add(field.asText());
                }
            }
            if (!lexicalFields.isEmpty()) {
                dto.setLexicalFields(lexicalFields);
            }
        }

        // Parse lexical_fields_translated map
        JsonNode lexicalFieldsTranslatedNode = jsonNode.path("lexical_fields_translated");
        if (!lexicalFieldsTranslatedNode.isMissingNode() && lexicalFieldsTranslatedNode.isObject()) {
            java.util.Map<String, java.util.List<String>> translatedMap = new java.util.HashMap<>();
            var fieldNames = lexicalFieldsTranslatedNode.fieldNames();
            while (fieldNames.hasNext()) {
                String langCode = fieldNames.next();
                JsonNode langArray = lexicalFieldsTranslatedNode.get(langCode);
                if (langArray.isArray()) {
                    java.util.List<String> translations = new java.util.ArrayList<>();
                    for (JsonNode item : langArray) {
                        if (item.isTextual()) {
                            translations.add(item.asText());
                        }
                    }
                    if (!translations.isEmpty()) {
                        translatedMap.put(langCode, translations);
                    }
                }
            }
            if (!translatedMap.isEmpty()) {
                dto.setLexicalFieldsTranslated(translatedMap);
            }
        }

        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getListId() {
        return listId;
    }

    public void setListId(UUID listId) {
        this.listId = listId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public String getWordText() {
        return wordText;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public UUID getWordAnalysisId() {
        return wordAnalysisId;
    }

    public void setWordAnalysisId(UUID wordAnalysisId) {
        this.wordAnalysisId = wordAnalysisId;
    }

    public WordAnalysisDetailsDto getAnalysisDetails() {
        return analysisDetails;
    }

    public void setAnalysisDetails(WordAnalysisDetailsDto analysisDetails) {
        this.analysisDetails = analysisDetails;
    }

    public String getAnalysisSource() {
        return analysisSource;
    }

    public void setAnalysisSource(String analysisSource) {
        this.analysisSource = analysisSource;
    }
}