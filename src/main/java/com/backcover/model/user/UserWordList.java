package com.backcover.model.user;

import com.backcover.model.Book;
import com.backcover.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_word_list")
public class UserWordList {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "list_name", nullable = false, length = 255)
    private String listName;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode = "fr"; // Default language

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book; // Optional - for book-specific lists

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public UserWordList() {
    }

    public UserWordList(User user, String listName, boolean isDefault) {
        this.user = user;
        this.listName = listName;
        this.isDefault = isDefault;
        this.languageCode = "fr"; // Default language
    }

    public UserWordList(User user, String listName, boolean isDefault, String languageCode) {
        this.user = user;
        this.listName = listName;
        this.isDefault = isDefault;
        this.languageCode = languageCode;
    }

    public UserWordList(User user, String listName, boolean isDefault, String languageCode, Book book) {
        this.user = user;
        this.listName = listName;
        this.isDefault = isDefault;
        this.languageCode = languageCode;
        this.book = book;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}