package com.backcover.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Table de liaison pour associer une instance de mot à une traduction spécifique et à son paragraphe.
 * Remplace l'ancienne table contextual_word_meaning en séparant les responsabilités.
 */
@Entity
@Table(name = "word_context",
       uniqueConstraints = {
           @UniqueConstraint(name = "ux_word_context_instance", 
                           columnNames = {"book_id", "page_number", "word_instance_id"})
       },
       indexes = {
           @Index(name = "idx_word_context_lookup", 
                  columnList = "book_id, page_number, word_instance_id"),
           @Index(name = "idx_word_context_translation", 
                  columnList = "word_translation_id"),
           @Index(name = "idx_word_context_analysis", 
                  columnList = "word_analysis_id")
       })
public class WordContext {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "word_instance_id", nullable = false, length = 100)
    private String wordInstanceId;

    @Column(name = "word_text_in_context", nullable = false, columnDefinition = "TEXT")
    private String wordTextInContext; // Le mot tel qu'il apparaît dans le texte

    @Column(name = "word_analysis_id", nullable = false)
    private UUID wordAnalysisId;

    @Column(name = "word_translation_id", nullable = false)
    private UUID wordTranslationId;

    @Column(name = "context_hash", nullable = false, length = 64)
    private String contextHash; // Référence vers ParagraphContext

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Relations JPA (optionnelles, peuvent être lazy-loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_translation_id", insertable = false, updatable = false)
    private WordTranslation wordTranslation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_analysis_id", insertable = false, updatable = false)
    private WordAnalysis wordAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "context_hash", insertable = false, updatable = false)
    private ParagraphContext paragraphContext;

    // Constructors
    public WordContext() {
    }

    public WordContext(UUID bookId, Integer pageNumber, String wordInstanceId, 
                      String wordTextInContext, UUID wordAnalysisId, 
                      UUID wordTranslationId, String contextHash) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordTextInContext = wordTextInContext;
        this.wordAnalysisId = wordAnalysisId;
        this.wordTranslationId = wordTranslationId;
        this.contextHash = contextHash;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
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

    public String getWordTextInContext() {
        return wordTextInContext;
    }

    public void setWordTextInContext(String wordTextInContext) {
        this.wordTextInContext = wordTextInContext;
    }

    public UUID getWordAnalysisId() {
        return wordAnalysisId;
    }

    public void setWordAnalysisId(UUID wordAnalysisId) {
        this.wordAnalysisId = wordAnalysisId;
    }

    public UUID getWordTranslationId() {
        return wordTranslationId;
    }

    public void setWordTranslationId(UUID wordTranslationId) {
        this.wordTranslationId = wordTranslationId;
    }

    public String getContextHash() {
        return contextHash;
    }

    public void setContextHash(String contextHash) {
        this.contextHash = contextHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public WordTranslation getWordTranslation() {
        return wordTranslation;
    }

    public void setWordTranslation(WordTranslation wordTranslation) {
        this.wordTranslation = wordTranslation;
    }

    public WordAnalysis getWordAnalysis() {
        return wordAnalysis;
    }

    public void setWordAnalysis(WordAnalysis wordAnalysis) {
        this.wordAnalysis = wordAnalysis;
    }

    public ParagraphContext getParagraphContext() {
        return paragraphContext;
    }

    public void setParagraphContext(ParagraphContext paragraphContext) {
        this.paragraphContext = paragraphContext;
    }

    // equals and hashCode based on business key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordContext that = (WordContext) o;
        return Objects.equals(bookId, that.bookId) &&
               Objects.equals(pageNumber, that.pageNumber) &&
               Objects.equals(wordInstanceId, that.wordInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, pageNumber, wordInstanceId);
    }

    @Override
    public String toString() {
        return "WordContext{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", wordInstanceId='" + wordInstanceId + '\'' +
                ", wordTextInContext='" + wordTextInContext + '\'' +
                ", wordAnalysisId=" + wordAnalysisId +
                ", wordTranslationId=" + wordTranslationId +
                ", contextHash='" + contextHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}