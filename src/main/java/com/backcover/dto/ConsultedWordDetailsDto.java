package com.backcover.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour représenter un mot consulté avec ses détails complets d'analyse et sa traduction.
 * Utilisé par l'endpoint GET /api/users/me/books/{bookId}/consulted-words-details
 */
public class ConsultedWordDetailsDto {

    // --- Informations du mot consulté ---
    private UUID wordContextId;
    private UUID wordListItemId;       // ID de l'item dans la word list pour suppression
    private String wordInstanceId;
    private String wordTextInContext;
    private Integer pageNumber;
    private LocalDateTime consultedAt;

    // --- Détails de l'analyse du mot ---
    private UUID wordAnalysisId;
    private String wordType;           // "اسم", "فعل", "حرف", "unknown"
    private String root;               // Racine du mot
    private String canonicalForm;      // Forme canonique (dictionnaire)
    private JsonNode analysisDetails;  // Détails spécifiques selon le type (singular/plural, madi/mudari, etc.)
    private String analysisSource;     // Source de l'analyse (ex: "Gemini-1.5-Flash")

    // --- Traduction dans la langue demandée ---
    private UUID wordTranslationId;
    private String translationText;    // Traduction dans la langue cible
    private String translationLanguageCode; // Code de la langue de traduction
    private Double translationConfidenceScore; // Score de confiance de la traduction
    private String translationSource; // Source de la traduction

    // --- Contexte du paragraphe ---
    private String paragraphText;      // Texte du paragraphe contenant le mot

    // Constructeur par défaut
    public ConsultedWordDetailsDto() {}

    // Constructeur complet
    public ConsultedWordDetailsDto(UUID wordContextId, UUID wordListItemId, String wordInstanceId, String wordTextInContext,
                                  Integer pageNumber, LocalDateTime consultedAt, UUID wordAnalysisId,
                                  String wordType, String root, String canonicalForm, JsonNode analysisDetails,
                                  String analysisSource, UUID wordTranslationId, String translationText,
                                  String translationLanguageCode, Double translationConfidenceScore,
                                  String translationSource, String paragraphText) {
        this.wordContextId = wordContextId;
        this.wordListItemId = wordListItemId;
        this.wordInstanceId = wordInstanceId;
        this.wordTextInContext = wordTextInContext;
        this.pageNumber = pageNumber;
        this.consultedAt = consultedAt;
        this.wordAnalysisId = wordAnalysisId;
        this.wordType = wordType;
        this.root = root;
        this.canonicalForm = canonicalForm;
        this.analysisDetails = analysisDetails;
        this.analysisSource = analysisSource;
        this.wordTranslationId = wordTranslationId;
        this.translationText = translationText;
        this.translationLanguageCode = translationLanguageCode;
        this.translationConfidenceScore = translationConfidenceScore;
        this.translationSource = translationSource;
        this.paragraphText = paragraphText;
    }

    // Getters and Setters
    public UUID getWordContextId() {
        return wordContextId;
    }

    public void setWordContextId(UUID wordContextId) {
        this.wordContextId = wordContextId;
    }

    public UUID getWordListItemId() {
        return wordListItemId;
    }

    public void setWordListItemId(UUID wordListItemId) {
        this.wordListItemId = wordListItemId;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public String getWordTextInContext() {
        return wordTextInContext;
    }

    public void setWordTextInContext(String wordTextInContext) {
        this.wordTextInContext = wordTextInContext;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public LocalDateTime getConsultedAt() {
        return consultedAt;
    }

    public void setConsultedAt(LocalDateTime consultedAt) {
        this.consultedAt = consultedAt;
    }

    public UUID getWordAnalysisId() {
        return wordAnalysisId;
    }

    public void setWordAnalysisId(UUID wordAnalysisId) {
        this.wordAnalysisId = wordAnalysisId;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getCanonicalForm() {
        return canonicalForm;
    }

    public void setCanonicalForm(String canonicalForm) {
        this.canonicalForm = canonicalForm;
    }

    public JsonNode getAnalysisDetails() {
        return analysisDetails;
    }

    public void setAnalysisDetails(JsonNode analysisDetails) {
        this.analysisDetails = analysisDetails;
    }

    public String getAnalysisSource() {
        return analysisSource;
    }

    public void setAnalysisSource(String analysisSource) {
        this.analysisSource = analysisSource;
    }

    public UUID getWordTranslationId() {
        return wordTranslationId;
    }

    public void setWordTranslationId(UUID wordTranslationId) {
        this.wordTranslationId = wordTranslationId;
    }

    public String getTranslationText() {
        return translationText;
    }

    public void setTranslationText(String translationText) {
        this.translationText = translationText;
    }

    public String getTranslationLanguageCode() {
        return translationLanguageCode;
    }

    public void setTranslationLanguageCode(String translationLanguageCode) {
        this.translationLanguageCode = translationLanguageCode;
    }

    public Double getTranslationConfidenceScore() {
        return translationConfidenceScore;
    }

    public void setTranslationConfidenceScore(Double translationConfidenceScore) {
        this.translationConfidenceScore = translationConfidenceScore;
    }

    public String getTranslationSource() {
        return translationSource;
    }

    public void setTranslationSource(String translationSource) {
        this.translationSource = translationSource;
    }

    public String getParagraphText() {
        return paragraphText;
    }

    public void setParagraphText(String paragraphText) {
        this.paragraphText = paragraphText;
    }

    @Override
    public String toString() {
        return "ConsultedWordDetailsDto{" +
                "wordContextId=" + wordContextId +
                ", wordInstanceId='" + wordInstanceId + '\'' +
                ", wordTextInContext='" + wordTextInContext + '\'' +
                ", pageNumber=" + pageNumber +
                ", consultedAt=" + consultedAt +
                ", wordAnalysisId=" + wordAnalysisId +
                ", wordType='" + wordType + '\'' +
                ", root='" + root + '\'' +
                ", canonicalForm='" + canonicalForm + '\'' +
                ", analysisSource='" + analysisSource + '\'' +
                ", wordTranslationId=" + wordTranslationId +
                ", translationText='" + translationText + '\'' +
                ", translationLanguageCode='" + translationLanguageCode + '\'' +
                ", translationConfidenceScore=" + translationConfidenceScore +
                ", translationSource='" + translationSource + '\'' +
                ", paragraphText='" + (paragraphText != null ? paragraphText.substring(0, Math.min(50, paragraphText.length())) + "..." : "null") + '\'' +
                '}';
    }
}