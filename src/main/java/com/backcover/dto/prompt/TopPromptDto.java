package com.backcover.dto.prompt;

import java.math.BigDecimal;

/**
 * DTO for top performing prompts in dashboard
 */
public class TopPromptDto {
    
    private String identifier;
    private String name;
    private String category;
    private long requestCount;
    private BigDecimal totalCost;
    private double successRate;
    private double averageResponseTime;
    private long totalTokens;
    private String mostUsedModel;
    
    // Getters and Setters
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public long getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
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
    
    public long getTotalTokens() {
        return totalTokens;
    }
    
    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }
    
    public String getMostUsedModel() {
        return mostUsedModel;
    }
    
    public void setMostUsedModel(String mostUsedModel) {
        this.mostUsedModel = mostUsedModel;
    }
}