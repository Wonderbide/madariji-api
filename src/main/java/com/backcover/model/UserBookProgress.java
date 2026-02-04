// Dans com/backcover/model/UserBookProgress.java
package com.backcover.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
// Pas besoin d'importer UUID ici directement si User et Book le font déjà et que UserBookProgressId l'utilise

@Entity
@Table(name = "user_book_progress")
public class UserBookProgress {

    @EmbeddedId
    private UserBookProgressId id; // id contiendra les UUIDs

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "last_read_page_number", nullable = false)
    private Integer lastReadPageNumber;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UserBookProgress() {
    }

    // Le constructeur utilisera les IDs UUID des entités User et Book
    public UserBookProgress(User user, Book book, Integer lastReadPageNumber) {
        // user.getId() et book.getId() retournent maintenant des UUID
        this.id = new UserBookProgressId(user.getId(), book.getId());
        this.user = user;
        this.book = book;
        this.lastReadPageNumber = lastReadPageNumber;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters et Setters (les types de retour pour getId().getUserId() et getId().getBookId() seront UUID)

    public UserBookProgressId getId() {
        return id;
    }

    public void setId(UserBookProgressId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getLastReadPageNumber() {
        return lastReadPageNumber;
    }

    public void setLastReadPageNumber(Integer lastReadPageNumber) {
        this.lastReadPageNumber = lastReadPageNumber;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    @PrePersist
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}