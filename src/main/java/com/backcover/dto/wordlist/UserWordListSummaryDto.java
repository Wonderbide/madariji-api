package com.backcover.dto.wordlist;

import com.backcover.model.user.UserWordList;

import java.time.Instant;
import java.util.UUID;

public class UserWordListSummaryDto {
    private UUID id;
    private String listName;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
    private long itemCount; // Nombre d'éléments dans la liste
    private UUID bookId; // ID du livre associé
    private String languageCode; // Code de langue de la liste
    private String bookTitle; // Titre du livre associé

    // Constructors
    public UserWordListSummaryDto() {
    }

    public UserWordListSummaryDto(UUID id, String listName, boolean isDefault, 
                                Instant createdAt, Instant updatedAt, long itemCount,
                                UUID bookId, String languageCode, String bookTitle) {
        this.id = id;
        this.listName = listName;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.itemCount = itemCount;
        this.bookId = bookId;
        this.languageCode = languageCode;
        this.bookTitle = bookTitle;
    }

    // Factory method to create DTO from entity
    public static UserWordListSummaryDto fromEntity(UserWordList entity, long itemCount) {
        if (entity == null) {
            return null;
        }
        
        return new UserWordListSummaryDto(
            entity.getId(),
            entity.getListName(),
            entity.isDefault(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            itemCount,
            entity.getBook() != null ? entity.getBook().getId() : null,
            entity.getLanguageCode(),
            entity.getBook() != null ? entity.getBook().getTitle() : null
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}