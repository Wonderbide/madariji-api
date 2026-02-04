package com.backcover.event;

import com.backcover.dto.page.StructuredPageDataDto;

import java.util.List;
import java.util.UUID;

public class BatchStructuredEvent {
    private final UUID bookId;
    private final List<StructuredPageDataDto> pages;
    private final Integer batchIndex;
    private final Integer startPageIndex;
    private final Integer endPageIndex;

    public BatchStructuredEvent(UUID bookId, List<StructuredPageDataDto> pages, 
                               Integer batchIndex, Integer startPageIndex, Integer endPageIndex) {
        this.bookId = bookId;
        this.pages = pages;
        this.batchIndex = batchIndex;
        this.startPageIndex = startPageIndex;
        this.endPageIndex = endPageIndex;
    }

    public UUID getBookId() { return bookId; }
    public List<StructuredPageDataDto> getPages() { return pages; }
    public Integer getBatchIndex() { return batchIndex; }
    public Integer getStartPageIndex() { return startPageIndex; }
    public Integer getEndPageIndex() { return endPageIndex; }

    @Override
    public String toString() {
        return "BatchStructuredEvent{" +
                "bookId=" + bookId +
                ", batchIndex=" + batchIndex +
                ", startPageIndex=" + startPageIndex +
                ", endPageIndex=" + endPageIndex +
                ", pageCount=" + (pages != null ? pages.size() : 0) +
                '}';
    }
}