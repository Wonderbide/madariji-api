package com.backcover.dto.prompt;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cost trend data point
 */
public class CostTrend {
    
    private LocalDateTime periodStart;
    private BigDecimal totalCost;
    private BigDecimal avgCost;
    private BigDecimal minCost;
    private BigDecimal maxCost;
    private Long requestCount;
    
    // Default constructor
    public CostTrend() {
    }
    
    // Constructor for query results
    public CostTrend(LocalDateTime periodStart, BigDecimal totalCost, BigDecimal avgCost,
                     BigDecimal minCost, BigDecimal maxCost, Long requestCount) {
        this.periodStart = periodStart;
        this.totalCost = totalCost;
        this.avgCost = avgCost;
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.requestCount = requestCount;
    }
    
    // Getters and setters
    public LocalDateTime getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public BigDecimal getAvgCost() {
        return avgCost;
    }
    
    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }
    
    public BigDecimal getMinCost() {
        return minCost;
    }
    
    public void setMinCost(BigDecimal minCost) {
        this.minCost = minCost;
    }
    
    public BigDecimal getMaxCost() {
        return maxCost;
    }
    
    public void setMaxCost(BigDecimal maxCost) {
        this.maxCost = maxCost;
    }
    
    public Long getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}