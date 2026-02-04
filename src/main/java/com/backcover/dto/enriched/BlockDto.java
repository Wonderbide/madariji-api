package com.backcover.dto.enriched;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class BlockDto {
    private String blockType;       // Type du bloc selon Gemini (H1, paragraph, etc.)
    private List<WordDto> words = new ArrayList<>(); // Liste des mots dans ce bloc (Initialisée ici)

    // Constructeur sans arguments
    public BlockDto() {
    }

    // Constructeur avec tous les arguments
    public BlockDto(String blockType, List<WordDto> words) {
        this.blockType = blockType;
        // S'assurer qu'on ne reçoit pas une liste null
        this.words = (words != null) ? words : new ArrayList<>();
    }

    // Getters
    public String getBlockType() {
        return blockType;
    }

    public List<WordDto> getWords() {
        // Optionnel: retourner une copie pour l'immutabilité externe, mais pas nécessaire pour un DTO simple
        return words;
    }

    // Setters
    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public void setWords(List<WordDto> words) {
        // S'assurer qu'on ne met pas une liste null
        this.words = (words != null) ? words : new ArrayList<>();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockDto blockDto = (BlockDto) o;
        return Objects.equals(blockType, blockDto.blockType) && Objects.equals(words, blockDto.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockType, words);
    }

    @Override
    public String toString() {
        // Attention: lister tous les mots peut rendre le log très long
        return "BlockDto{" +
                "blockType='" + blockType + '\'' +
                ", words=[size=" + (words != null ? words.size() : 0) + "]" +
                '}';
    }
}