package com.backcover.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Référentiel des traductions/définitions distinctes pour les analyses de mots.
 * Permet de stocker plusieurs traductions différentes pour un même word_analysis_id et langue,
 * tout en évitant la duplication des traductions textuellement identiques.
 */
@Entity
@Table(name = "word_translation", 
       uniqueConstraints = {
           @UniqueConstraint(name = "ux_word_translation_unique", 
                           columnNames = {"word_analysis_id", "language_code", "translation_text"})
       },
       indexes = {
           @Index(name = "idx_word_translation_lookup", 
                  columnList = "word_analysis_id, language_code"),
           @Index(name = "idx_word_translation_confidence", 
                  columnList = "confidence_score DESC")
       })
public class WordTranslation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "word_analysis_id", nullable = false)
    private UUID wordAnalysisId;

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode;

    @Column(name = "translation_text", nullable = false, columnDefinition = "TEXT")
    private String translationText;

    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.00-1.00

    @Column(name = "source", length = 50)
    private String source; // 'gemini', 'manual', 'google_translate', etc.

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public WordTranslation() {
    }

    public WordTranslation(UUID wordAnalysisId, String languageCode, String translationText, 
                          Double confidenceScore, String source) {
        this.wordAnalysisId = wordAnalysisId;
        this.languageCode = languageCode;
        this.translationText = translationText;
        this.confidenceScore = confidenceScore;
        this.source = source;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWordAnalysisId() {
        return wordAnalysisId;
    }

    public void setWordAnalysisId(UUID wordAnalysisId) {
        this.wordAnalysisId = wordAnalysisId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTranslationText() {
        return translationText;
    }

    public void setTranslationText(String translationText) {
        this.translationText = translationText;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // equals and hashCode based on business key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordTranslation that = (WordTranslation) o;
        return Objects.equals(wordAnalysisId, that.wordAnalysisId) &&
               Objects.equals(languageCode, that.languageCode) &&
               Objects.equals(translationText, that.translationText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordAnalysisId, languageCode, translationText);
    }

    @Override
    public String toString() {
        return "WordTranslation{" +
                "id=" + id +
                ", wordAnalysisId=" + wordAnalysisId +
                ", languageCode='" + languageCode + '\'' +
                ", translationText='" + (translationText != null ? translationText.substring(0, Math.min(translationText.length(), 50)) + "..." : "null") + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", source='" + source + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}