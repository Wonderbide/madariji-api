package com.backcover.dto.enriched;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class PageDto {
    private int pageNumber; // Numéro de page (base 0 ou 1 selon votre convention)
    private List<BlockDto> content = new ArrayList<>(); // Liste des blocs dans la page (Initialisée ici)

    // Constructeur sans arguments
    public PageDto() {
    }

    // Constructeur avec tous les arguments
    public PageDto(int pageNumber, List<BlockDto> content) {
        this.pageNumber = pageNumber;
        // S'assurer qu'on ne reçoit pas une liste null
        this.content = (content != null) ? content : new ArrayList<>();
    }

    // Getters
    public int getPageNumber() {
        return pageNumber;
    }

    public List<BlockDto> getContent() {
        return content;
    }

    // Setters
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setContent(List<BlockDto> content) {
        // S'assurer qu'on ne met pas une liste null
        this.content = (content != null) ? content : new ArrayList<>();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageDto pageDto = (PageDto) o;
        return pageNumber == pageDto.pageNumber && Objects.equals(content, pageDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, content);
    }

    @Override
    public String toString() {
        // Attention: lister tous les blocs peut rendre le log très long
        return "PageDto{" +
                "pageNumber=" + pageNumber +
                ", content=[size=" + (content != null ? content.size() : 0) + "]" +
                '}';
    }
}