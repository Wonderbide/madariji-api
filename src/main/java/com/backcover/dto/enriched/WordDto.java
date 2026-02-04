package com.backcover.dto.enriched;

import java.util.Objects;

public class WordDto {
    private String id;   // Nouvel ID généré (ex: "p0b1w5")
    private String text; // Texte du mot AVEC Tashkeel

    // Constructeur sans arguments
    public WordDto() {
    }

    // Constructeur avec tous les arguments
    public WordDto(String id, String text) {
        this.id = id;
        this.text = text;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    // equals, hashCode, toString (Générés par IDE ou manuellement)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordDto wordDto = (WordDto) o;
        return Objects.equals(id, wordDto.id) && Objects.equals(text, wordDto.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }

    @Override
    public String toString() {
        return "WordDto{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}