// Dans com/backcover/event/TextStructuredEvent.java
package com.backcover.event;

import com.backcover.dto.page.StructuredPageDataDto; // Assurez-vous de l'import
import java.util.List;
import java.util.UUID;

public class TextStructuredEvent {
    private final UUID bookId;
    private final List<StructuredPageDataDto> structuredPages;

    public TextStructuredEvent(UUID bookId, List<StructuredPageDataDto> structuredPages) {
        this.bookId = bookId;
        this.structuredPages = structuredPages;
    }
    // Getters
    public UUID getBookId() { return bookId; }
    public List<StructuredPageDataDto> getStructuredPages() { return structuredPages; }
}