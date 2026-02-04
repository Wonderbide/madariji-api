package com.backcover.dto.wordlist;

import com.backcover.model.user.UserWordListItem;

import java.time.Instant;
import java.util.UUID;

public class UserWordListItemDto {
    private UUID id;
    private UUID bookId;
    private String bookTitle;
    private Integer pageNumber;
    private String wordInstanceId;
    private String wordText;
    private Instant addedAt;

    // Constructors
    public UserWordListItemDto() {
    }

    public UserWordListItemDto(UUID id, UUID bookId, String bookTitle, Integer pageNumber, 
                             String wordInstanceId, String wordText, Instant addedAt) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordText = wordText;
        this.addedAt = addedAt;
    }

    // Factory method to create DTO from entity
    public static UserWordListItemDto fromEntity(UserWordListItem entity) {
        if (entity == null) {
            return null;
        }
        
        return new UserWordListItemDto(
            entity.getId(),
            entity.getBook().getId(),
            entity.getBook().getTitle(),
            entity.getPageNumber(),
            entity.getWordInstanceId(),
            entity.getWordText(),
            entity.getAddedAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public String getWordText() {
        return wordText;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}