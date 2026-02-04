package com.backcover.dto.prompt;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating prompt templates
 */
public class UpdatePromptTemplateDto {
    
    
    private String promptContent;
    
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
    
    // targetLanguageCode removed from database schema
    
    private Boolean isActive;
    
    // Getters and Setters
    
    
    public String getPromptContent() {
        return promptContent;
    }
    
    public void setPromptContent(String promptContent) {
        this.promptContent = promptContent;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}