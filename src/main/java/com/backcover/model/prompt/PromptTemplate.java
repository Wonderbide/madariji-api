package com.backcover.model.prompt;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing prompt templates
 * Matches AI_CONFIG_MINIMAL.md specification exactly
 */
@Entity
@Table(name = "prompt_library", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"prompt_key", "version"}))
public class PromptTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "prompt_key", nullable = false, length = 100)
    private String promptKey;
    
    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "version", nullable = false, length = 20)
    private String version;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    public String getPromptText() {
        return promptText;
    }
    
    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Temporary compatibility methods - These will be removed once all code is updated
    // For now, they map to the new column names
    public String getIdentifier() {
        return promptKey;
    }
    
    public void setIdentifier(String identifier) {
        this.promptKey = identifier;
    }
    
    public String getPromptContent() {
        return promptText;
    }
    
    public void setPromptContent(String promptContent) {
        this.promptText = promptContent;
    }
}