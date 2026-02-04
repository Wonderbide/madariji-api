// src/main/java/com/backcover/dto/page/StructuredPageDataDto.java
package com.backcover.dto.page;

import java.util.List;
import java.util.ArrayList;

public class StructuredPageDataDto {
    private int pageNumber; // Index de la page
    private List<PageParagraphDto> content = new ArrayList<>(); // Initialise la liste

    // Constructeurs, Getters, Setters
    public StructuredPageDataDto() {}

    public StructuredPageDataDto(int pageNumber) { this.pageNumber = pageNumber; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public List<PageParagraphDto> getContent() { return content; }
    public void setContent(List<PageParagraphDto> content) { this.content = content; }
    public void addParagraph(PageParagraphDto paragraph) { this.content.add(paragraph); } // Méthode pratique
}