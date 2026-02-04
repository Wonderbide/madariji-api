package com.backcover.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Référentiel des contextes de paragraphe uniques.
 * Permet de dédupliquer le stockage des textes de paragraphe identiques.
 */
@Entity
@Table(name = "paragraph_context",
       indexes = {
           @Index(name = "idx_paragraph_context_hash", columnList = "context_hash")
       })
public class ParagraphContext {

    @Id
    @Column(name = "context_hash", length = 64, nullable = false)
    private String contextHash; // SHA-256 hash du paragraph_text

    @Column(name = "paragraph_text", nullable = false, columnDefinition = "TEXT")
    private String paragraphText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ParagraphContext() {
    }

    public ParagraphContext(String contextHash, String paragraphText) {
        this.contextHash = contextHash;
        this.paragraphText = paragraphText;
    }

    // Getters and Setters
    public String getContextHash() {
        return contextHash;
    }

    public void setContextHash(String contextHash) {
        this.contextHash = contextHash;
    }

    public String getParagraphText() {
        return paragraphText;
    }

    public void setParagraphText(String paragraphText) {
        this.paragraphText = paragraphText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // equals and hashCode based on hash
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParagraphContext that = (ParagraphContext) o;
        return Objects.equals(contextHash, that.contextHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextHash);
    }

    @Override
    public String toString() {
        return "ParagraphContext{" +
                "contextHash='" + contextHash + '\'' +
                ", paragraphText='" + (paragraphText != null ? paragraphText.substring(0, Math.min(paragraphText.length(), 100)) + "..." : "null") + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}