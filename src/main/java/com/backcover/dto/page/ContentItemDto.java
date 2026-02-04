// src/main/java/com/backcover/dto/page/ContentItemDto.java
package com.backcover.dto.page;

import com.fasterxml.jackson.annotation.JsonInclude; // Pour ne pas inclure les nulls dans le JSON

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut pas les champs null (comme id pour non-mots)
public class ContentItemDto {
    private String id;    // Ex: "p0w5" - Présent seulement pour type='word'
    private String type;  // Ex: "word", "space", "punctuation"
    private String text;  // Le texte brut

    // Constructeurs, Getters, Setters
    public ContentItemDto() {}

    public ContentItemDto(String type, String text, String id) {
        this.type = type;
        this.text = text;
        this.id = id; // Sera null si pas un mot
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
