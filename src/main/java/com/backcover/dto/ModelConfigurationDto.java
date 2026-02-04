package com.backcover.dto;

/**
 * DTO for AI model configuration including temperature and max tokens
 */
public class ModelConfigurationDto {
    private String modelId;
    private Double temperature;
    private Integer maxTokens;
    
    public ModelConfigurationDto(String modelId, Double temperature, Integer maxTokens) {
        this.modelId = modelId;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxOutputTokens() {
        return maxTokens;
    }
    
    public void setMaxOutputTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}