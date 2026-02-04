package com.backcover.dto.prompt;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for prompt version information
 */
public class PromptVersionDto {
    
    private UUID id;
    private String identifier;
    private String version;
    private String name;
    private String description;
    private boolean isActive;
    private boolean isDeprecated;
    private String changeDescription;
    private String migrationNotes;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime deprecatedAt;
    private String deprecatedBy;
    private PromptVersionMetrics metrics;
    
    public static class PromptVersionMetrics {
        private long totalRequests;
        private double successRate;
        private double averageResponseTime;
        private long totalCost;
        private int qualityIssues;
        
        // Getters and Setters
        
        public long getTotalRequests() {
            return totalRequests;
        }
        
        public void setTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
        }
        
        public double getSuccessRate() {
            return successRate;
        }
        
        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
        
        public double getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }
        
        public long getTotalCost() {
            return totalCost;
        }
        
        public void setTotalCost(long totalCost) {
            this.totalCost = totalCost;
        }
        
        public int getQualityIssues() {
            return qualityIssues;
        }
        
        public void setQualityIssues(int qualityIssues) {
            this.qualityIssues = qualityIssues;
        }
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isDeprecated() {
        return isDeprecated;
    }
    
    public void setDeprecated(boolean deprecated) {
        isDeprecated = deprecated;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getDeprecatedAt() {
        return deprecatedAt;
    }
    
    public void setDeprecatedAt(LocalDateTime deprecatedAt) {
        this.deprecatedAt = deprecatedAt;
    }
    
    public String getDeprecatedBy() {
        return deprecatedBy;
    }
    
    public void setDeprecatedBy(String deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }
    
    public PromptVersionMetrics getMetrics() {
        return metrics;
    }
    
    public void setMetrics(PromptVersionMetrics metrics) {
        this.metrics = metrics;
    }
}