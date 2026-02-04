// Dans com/backcover/model/UserBookProgressId.java
package com.backcover.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID; // Importation nécessaire

@Embeddable
public class UserBookProgressId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private UUID userId; // <<< Changé en UUID

    @Column(name = "book_id")
    private UUID bookId; // <<< Changé en UUID

    public UserBookProgressId() {
    }

    public UserBookProgressId(UUID userId, UUID bookId) { // <<< Changé en UUID
        this.userId = userId;
        this.bookId = bookId;
    }

    // Getters, Setters
    public UUID getUserId() { // <<< Changé en UUID
        return userId;
    }

    public void setUserId(UUID userId) { // <<< Changé en UUID
        this.userId = userId;
    }

    public UUID getBookId() { // <<< Changé en UUID
        return bookId;
    }

    public void setBookId(UUID bookId) { // <<< Changé en UUID
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBookProgressId that = (UserBookProgressId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bookId);
    }
}