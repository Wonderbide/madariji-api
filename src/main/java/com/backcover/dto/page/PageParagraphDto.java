package com.backcover.dto.page; // Adapte le package

import java.util.List;
import java.util.ArrayList;

public class PageParagraphDto {
    private String type = "paragraph"; // Type mis à jour par Gemini (heading1, p, etc.)
    private List<ContentItemDto> items = new ArrayList<>(); // Structure originale Vision avec IDs
    private String enrichedText; // <<<--- NOUVEAU CHAMP pour texte avec Tashkeel

    // Constructeurs, Getters, Setters
    public PageParagraphDto() {}

    public PageParagraphDto(String type) { this.type = type; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<ContentItemDto> getItems() { return items; }
    public void setItems(List<ContentItemDto> items) { this.items = items; }
    public void addItem(ContentItemDto item) { this.items.add(item); }

    // Getter/Setter pour le nouveau champ
    public String getEnrichedText() { return enrichedText; }
    public void setEnrichedText(String enrichedText) { this.enrichedText = enrichedText; }
}