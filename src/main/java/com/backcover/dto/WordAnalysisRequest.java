// src/main/java/com/backcover/dto/WordAnalysisRequest.java
package com.backcover.dto;

import java.util.UUID; // Importer UUID

/**
 * Data Transfer Object (DTO) pour les requêtes d'analyse de mot contextuelle.
 * Contient les informations nécessaires pour identifier l'instance exacte du mot
 * et fournir le contexte (maintenant le paragraphe) à l'analyse.
 */
public class WordAnalysisRequest {

    // --- Champs de Contexte Essentiels ---
    private UUID bookId;         // Identifiant unique du livre
    private Integer pageNumber;     // Index de la page (probablement base 0)
    private String wordInstanceId; // Identifiant unique de l'instance du mot sur la page (ex: "p0b0w5")

    // --- Champs Informatifs / Pour LLM et Sauvegarde ---
    private String wordText;       // Le texte exact du mot cliqué (pour validation/prompt)
    // private String pageText;    // Supprimé - Remplacé par le texte du paragraphe
    private String paragraphText;  // <<< NOUVEAU CHAMP : Le texte complet du paragraphe contenant le mot
    private String bookTitle;      // Titre du livre (contexte pour LLM)
    private String targetLanguageCode; // Code de la langue de traduction cible (ex: 'fr', 'en', 'ar')

    // --- Constructeur par défaut (requis pour la désérialisation JSON) ---
    public WordAnalysisRequest() {}

    // --- Getters ---

    public UUID getBookId() {
        return bookId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public String getWordText() {
        return wordText;
    }

    // Ancien getter supprimé
    // public String getPageText() { return pageText; }

    public String getParagraphText() { // <<< NOUVEAU GETTER
        return paragraphText;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    // --- Setters (requis pour la désérialisation JSON par Jackson/Spring) ---

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    // Ancien setter supprimé
    // public void setPageText(String pageText) { this.pageText = pageText; }

    public void setParagraphText(String paragraphText) { // <<< NOUVEAU SETTER
        this.paragraphText = paragraphText;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    // --- toString() pour le débogage ---
    @Override
    public String toString() {
        return "WordAnalysisRequest{" +
                "bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", wordInstanceId='" + wordInstanceId + '\'' +
                ", wordText='" + wordText + '\'' +
                ", paragraphText='" + (paragraphText != null ? paragraphText.substring(0, Math.min(paragraphText.length(), 50)) + "..." : "null") + '\'' + // Tronqué pour log
                ", bookTitle='" + bookTitle + '\'' +
                ", targetLanguageCode='" + targetLanguageCode + '\'' +
                '}';
    }
}