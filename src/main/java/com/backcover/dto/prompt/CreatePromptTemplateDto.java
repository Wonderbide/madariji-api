package com.backcover.dto.prompt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating prompt templates
 */
public class CreatePromptTemplateDto {
    
    @NotBlank(message = "Identifier is required")
    @Size(max = 100, message = "Identifier cannot exceed 100 characters")
    private String identifier;
    
    
    @NotBlank(message = "Prompt content is required")
    private String promptContent;
    
    @NotBlank(message = "Version is required")
    @Size(max = 20, message = "Version cannot exceed 20 characters")
    private String version;
    
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
    
    // targetLanguageCode removed - not in database schema
    
    
    // Getters and Setters
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    
    public String getPromptContent() {
        return promptContent;
    }
    
    public void setPromptContent(String promptContent) {
        this.promptContent = promptContent;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
}