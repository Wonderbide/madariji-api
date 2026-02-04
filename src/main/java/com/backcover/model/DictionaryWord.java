package com.backcover.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente un mot unique dans le dictionnaire, indépendamment du contexte,
 * associé à une langue spécifique.
 */
@Entity
@Table(name = "dictionary_word",
        uniqueConstraints = {
                // Assure qu'une combinaison de mot et langue est unique
                @UniqueConstraint(columnNames = {"word_text", "language_code"})
        })
public class DictionaryWord {

    @Id
    @GeneratedValue(generator = "UUID") // Ou stratégie par défaut si UUID est bien géré
    private UUID id;

    @Column(name = "word_text", nullable = false, columnDefinition = "TEXT") // Utiliser TEXT pour les mots potentiellement longs
    private String wordText; // La forme normalisée/canonique du mot

    @Column(name = "language_code", nullable = false, length = 10) // Ex: 'ar', 'fr', 'en'
    private String languageCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructeur par défaut requis par JPA
    public DictionaryWord() {
        this.createdAt = LocalDateTime.now(); // Initialise à la création
    }

    // Constructeur pratique (optionnel)
    public DictionaryWord(String wordText, String languageCode) {
        this(); // Appelle le constructeur par défaut pour createdAt
        this.wordText = wordText;
        this.languageCode = languageCode;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getWordText() { return wordText; }
    public String getLanguageCode() { return languageCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setWordText(String wordText) { this.wordText = wordText; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    // Pas de setters pour id et createdAt (géré par JPA et initialisation)

    // --- equals() et hashCode() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryWord that = (DictionaryWord) o;
        return Objects.equals(wordText, that.wordText) &&
                Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordText, languageCode);
    }
}