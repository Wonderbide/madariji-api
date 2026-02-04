package com.backcover.dto.prompt;

/**
 * DTO for comparing two versions of a prompt
 */
public class PromptVersionComparisonDto {
    
    private PromptVersionDto version1;
    private PromptVersionDto version2;
    private boolean contentChanged;
    private boolean modelChanged;
    private boolean parametersChanged;
    private PerformanceComparison performanceComparison;
    
    public static class PerformanceComparison {
        private double successRateDiff;
        private double responseTimeDiff;
        private long costDiff;
        private int qualityIssuesDiff;
        private String recommendation;
        
        // Getters and Setters
        
        public double getSuccessRateDiff() {
            return successRateDiff;
        }
        
        public void setSuccessRateDiff(double successRateDiff) {
            this.successRateDiff = successRateDiff;
        }
        
        public double getResponseTimeDiff() {
            return responseTimeDiff;
        }
        
        public void setResponseTimeDiff(double responseTimeDiff) {
            this.responseTimeDiff = responseTimeDiff;
        }
        
        public long getCostDiff() {
            return costDiff;
        }
        
        public void setCostDiff(long costDiff) {
            this.costDiff = costDiff;
        }
        
        public int getQualityIssuesDiff() {
            return qualityIssuesDiff;
        }
        
        public void setQualityIssuesDiff(int qualityIssuesDiff) {
            this.qualityIssuesDiff = qualityIssuesDiff;
        }
        
        public String getRecommendation() {
            return recommendation;
        }
        
        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }
    
    // Getters and Setters
    
    public PromptVersionDto getVersion1() {
        return version1;
    }
    
    public void setVersion1(PromptVersionDto version1) {
        this.version1 = version1;
    }
    
    public PromptVersionDto getVersion2() {
        return version2;
    }
    
    public void setVersion2(PromptVersionDto version2) {
        this.version2 = version2;
    }
    
    public boolean isContentChanged() {
        return contentChanged;
    }
    
    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }
    
    public boolean isModelChanged() {
        return modelChanged;
    }
    
    public void setModelChanged(boolean modelChanged) {
        this.modelChanged = modelChanged;
    }
    
    public boolean isParametersChanged() {
        return parametersChanged;
    }
    
    public void setParametersChanged(boolean parametersChanged) {
        this.parametersChanged = parametersChanged;
    }
    
    public PerformanceComparison getPerformanceComparison() {
        return performanceComparison;
    }
    
    public void setPerformanceComparison(PerformanceComparison performanceComparison) {
        this.performanceComparison = performanceComparison;
    }
}