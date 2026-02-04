package com.backcover.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "book_metadata_translation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "language_code"}))
public class BookMetadataTranslation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "created_at")
    private Instant createdAt;

    public BookMetadataTranslation() {}

    public BookMetadataTranslation(Book book, String languageCode) {
        this.id = UUID.randomUUID();
        this.book = book;
        this.languageCode = languageCode;
        this.createdAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookMetadataTranslation that = (BookMetadataTranslation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookMetadataTranslation{" +
                "id=" + id +
                ", languageCode='" + languageCode + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
