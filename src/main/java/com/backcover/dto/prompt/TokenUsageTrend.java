package com.backcover.dto.prompt;

import java.time.LocalDateTime;

/**
 * Token usage trend data point
 */
public class TokenUsageTrend {
    
    private LocalDateTime periodStart;
    private Long totalInputTokens;
    private Long totalOutputTokens;
    private Long totalTokens;
    private Double avgInputTokens;
    private Double avgOutputTokens;
    private Long requestCount;
    
    // Default constructor
    public TokenUsageTrend() {
    }
    
    // Constructor for query results
    public TokenUsageTrend(LocalDateTime periodStart, Long totalInputTokens, Long totalOutputTokens,
                          Long totalTokens, Double avgInputTokens, Double avgOutputTokens, Long requestCount) {
        this.periodStart = periodStart;
        this.totalInputTokens = totalInputTokens;
        this.totalOutputTokens = totalOutputTokens;
        this.totalTokens = totalTokens;
        this.avgInputTokens = avgInputTokens;
        this.avgOutputTokens = avgOutputTokens;
        this.requestCount = requestCount;
    }
    
    // Getters and setters
    public LocalDateTime getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }
    
    public Long getTotalInputTokens() {
        return totalInputTokens;
    }
    
    public void setTotalInputTokens(Long totalInputTokens) {
        this.totalInputTokens = totalInputTokens;
    }
    
    public Long getTotalOutputTokens() {
        return totalOutputTokens;
    }
    
    public void setTotalOutputTokens(Long totalOutputTokens) {
        this.totalOutputTokens = totalOutputTokens;
    }
    
    public Long getTotalTokens() {
        return totalTokens;
    }
    
    public void setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
    }
    
    public Double getAvgInputTokens() {
        return avgInputTokens;
    }
    
    public void setAvgInputTokens(Double avgInputTokens) {
        this.avgInputTokens = avgInputTokens;
    }
    
    public Double getAvgOutputTokens() {
        return avgOutputTokens;
    }
    
    public void setAvgOutputTokens(Double avgOutputTokens) {
        this.avgOutputTokens = avgOutputTokens;
    }
    
    public Long getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}