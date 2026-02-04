// src/main/java/com/backcover/model/ContextualWordMeaning.java
package com.backcover.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode; // Pour JSONB si tu utilises request_context_details
import org.hibernate.type.SqlTypes;          // Pour JSONB

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente le sens et la traduction spécifiques d'une instance de mot
 * dans un contexte donné (livre, page, position).
 * C'est le cœur du cache de définitions contextuelles.
 */
@Entity
@Table(name = "contextual_word_meaning",
        indexes = { // Ajout d'index via l'annotation @Table pour correspondre au Liquibase
                @Index(name = "idx_contextual_meaning_lookup", columnList = "book_id, page_number, word_instance_id, translation_language_code")
        }
        // La contrainte unique est plus complexe à définir ici si besoin,
        // mieux gérée directement en Liquibase ou via une logique applicative.
)
public class ContextualWordMeaning {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId; // Pas de @ManyToOne vers Book ici pour garder découplé, mais pourrait être ajouté

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber; // Index de la page (base 0 ?)

    @Column(name = "word_instance_id", nullable = false, length = 100) // Ex: "p0b0w5"
    private String wordInstanceId;

    // --- Relation vers l'analyse structurelle ---
    @ManyToOne(fetch = FetchType.LAZY) // LAZY est généralement préférable ici
    @JoinColumn(name = "word_analysis_id", nullable = false)
    private WordAnalysis wordAnalysis; // Référence vers l'analyse associée

    @Column(name = "word_text_in_context", nullable = false, columnDefinition = "TEXT")
    private String wordTextInContext; // Le mot exact vu sur la page (avec Tashkeel)

    // --- NOUVEAU CHAMP POUR LE CONTEXTE PARAGRAPHE ---
    @Column(name = "paragraph_text", nullable = false, columnDefinition = "TEXT")
    private String paragraphText; // Le texte complet du paragraphe contenant le mot

    @Column(name = "translation_language_code", nullable = false, length = 10) // Ex: "fr"
    private String translationLanguageCode;

    @Column(name = "translation_text", nullable = false, columnDefinition = "TEXT")
    private String translationText; // La traduction dans ce contexte

    @JdbcTypeCode(SqlTypes.JSON) // Annotation pour gérer le type JSONB
    @Column(name = "request_context_details", columnDefinition = "jsonb", nullable = true)
    private String requestContextDetails; // Stocke d'autres détails optionnels du contexte de la requête

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructeur par défaut
    public ContextualWordMeaning() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public UUID getBookId() { return bookId; }
    public Integer getPageNumber() { return pageNumber; }
    public String getWordInstanceId() { return wordInstanceId; }
    public WordAnalysis getWordAnalysis() { return wordAnalysis; }
    public String getWordTextInContext() { return wordTextInContext; }
    public String getParagraphText() { return paragraphText; } // Getter pour le nouveau champ
    public String getTranslationLanguageCode() { return translationLanguageCode; }
    public String getTranslationText() { return translationText; }
    public String getRequestContextDetails() { return requestContextDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---

    // public void setId(UUID id) { this.id = id; } // Géré par JPA
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    public void setWordInstanceId(String wordInstanceId) { this.wordInstanceId = wordInstanceId; }
    public void setWordAnalysis(WordAnalysis wordAnalysis) { this.wordAnalysis = wordAnalysis; }
    public void setWordTextInContext(String wordTextInContext) { this.wordTextInContext = wordTextInContext; }
    public void setParagraphText(String paragraphText) { this.paragraphText = paragraphText; } // Setter pour le nouveau champ
    public void setTranslationLanguageCode(String translationLanguageCode) { this.translationLanguageCode = translationLanguageCode; }
    public void setTranslationText(String translationText) { this.translationText = translationText; }
    public void setRequestContextDetails(String requestContextDetails) { this.requestContextDetails = requestContextDetails; }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // Géré à la création

    // --- equals() et hashCode() ---
    // Basé sur la clé métier qui définit l'unicité d'un sens contextuel:
    // livre, page, instance de mot, et langue de traduction.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextualWordMeaning that = (ContextualWordMeaning) o;
        return Objects.equals(bookId, that.bookId) &&
                Objects.equals(pageNumber, that.pageNumber) &&
                Objects.equals(wordInstanceId, that.wordInstanceId) &&
                Objects.equals(translationLanguageCode, that.translationLanguageCode);
        // Note: On n'inclut pas wordAnalysis, translationText ou paragraphText ici,
        // car on veut identifier une *demande* de sens unique, pas le résultat lui-même.
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, pageNumber, wordInstanceId, translationLanguageCode);
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "ContextualWordMeaning{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", wordInstanceId='" + wordInstanceId + '\'' +
                ", wordAnalysisId=" + (wordAnalysis != null ? wordAnalysis.getId() : null) + // Affiche l'ID lié
                ", wordTextInContext='" + wordTextInContext + '\'' +
                ", paragraphText='" + (paragraphText != null ? paragraphText.substring(0, Math.min(paragraphText.length(), 50)) + "..." : "null") + '\'' + // Tronqué pour log
                ", translationLanguageCode='" + translationLanguageCode + '\'' +
                ", translationText='" + translationText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}