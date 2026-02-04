package com.backcover.dto; // Nouveau package ?
import java.util.List;
public class FinalPageDataDto {
    private int pageNumber; // 0-basé ou 1-basé selon ce que tu préfères stocker
    private List<TextBlockDto> blocks;
    // Getters/Setters


    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<TextBlockDto> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<TextBlockDto> blocks) {
        this.blocks = blocks;
    }
}