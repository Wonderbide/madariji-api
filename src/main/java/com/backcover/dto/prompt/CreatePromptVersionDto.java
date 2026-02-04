package com.backcover.dto.prompt;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new prompt version
 */
public class CreatePromptVersionDto {
    
    @NotBlank(message = "Version is required")
    private String version;
    
    
    @NotBlank(message = "Prompt content is required")
    private String promptContent;
    
    private String category;
    // targetLanguageCode removed - not in database schema
    
    @NotBlank(message = "Change description is required")
    private String changeDescription;
    
    private String migrationNotes;
    private boolean makeActive = true;
    private boolean deprecatePrevious = false;
    
    // Getters and Setters
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    
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
    
    
    public String getChangeDescription() {
        return changeDescription;
    }
    
    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }
    
    public String getMigrationNotes() {
        return migrationNotes;
    }
    
    public void setMigrationNotes(String migrationNotes) {
        this.migrationNotes = migrationNotes;
    }
    
    public boolean isMakeActive() {
        return makeActive;
    }
    
    public void setMakeActive(boolean makeActive) {
        this.makeActive = makeActive;
    }
    
    public boolean isDeprecatePrevious() {
        return deprecatePrevious;
    }
    
    public void setDeprecatePrevious(boolean deprecatePrevious) {
        this.deprecatePrevious = deprecatePrevious;
    }
}