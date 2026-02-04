package com.backcover.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente une analyse structurelle spécifique (type, racine, détails morphologiques)
 * pour un mot donné du dictionnaire (DictionaryWord).
 * Une même DictionaryWord peut avoir plusieurs WordAnalysis si différentes sources
 * ou versions d'analyse existent.
 */
@Entity
@Table(name = "word_analysis", indexes = {
        @Index(name = "idx_word_analysis_dict_id", columnList = "dictionary_word_id") // Index ajouté via annotation
})
public class WordAnalysis {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    // --- Relation vers le mot du dictionnaire ---
    // On garde juste l'ID ici pour l'instant, comme pour bookId dans ContextualWordMeaning.
    // On pourrait ajouter @ManyToOne si nécessaire, mais garder l'ID est plus simple.
    @Column(name = "dictionary_word_id", nullable = false)
    private UUID dictionaryWordId;

    // --- Données de l'analyse ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_data", columnDefinition = "jsonb", nullable = false)
    private String analysisData; // Stocke le JSON de l'analyse structurelle comme String

    @Column(name = "source", length = 50, nullable = true) // D'où vient l'analyse (LLM, manuel...)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructeur par défaut
    public WordAnalysis() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getDictionaryWordId() {
        return dictionaryWordId;
    }

    /**
     * Récupère les données JSON de l'analyse structurelle (type, racine, détails).
     * @return La chaîne JSON contenant les données d'analyse.
     */
    public String getAnalysisData() { // <<<=== LE GETTER MANQUANT ===>>>
        return analysisData;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- Setters ---

    // public void setId(UUID id) { this.id = id; } // Géré par JPA
    public void setDictionaryWordId(UUID dictionaryWordId) {
        this.dictionaryWordId = dictionaryWordId;
    }

    public void setAnalysisData(String analysisData) {
        this.analysisData = analysisData;
    }

    public void setSource(String source) {
        this.source = source;
    }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // Géré à la création

    // --- equals() et hashCode() ---
    // Basé sur les champs qui définissent une analyse unique pour un mot :
    // l'ID du mot et le contenu JSON de l'analyse.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordAnalysis that = (WordAnalysis) o;
        // Compare sur l'ID du mot lié et le contenu exact de l'analyse
        return Objects.equals(dictionaryWordId, that.dictionaryWordId) &&
                Objects.equals(analysisData, that.analysisData); // Comparaison de String JSON
    }

    @Override
    public int hashCode() {
        return Objects.hash(dictionaryWordId, analysisData);
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "WordAnalysis{" +
                "id=" + id +
                ", dictionaryWordId=" + dictionaryWordId +
                ", analysisData='" + (analysisData != null ? analysisData.substring(0, Math.min(analysisData.length(), 100)) + "..." : "null") + '\'' + // Tronqué
                ", source='" + source + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}