package com.backcover.model.prompt;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing AI model configurations
 * Matches AI_CONFIG_MINIMAL.md specification exactly
 */
@Entity
@Table(name = "llm_models")
public class ModelConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "model_code", nullable = false, unique = true, length = 50)
    private String modelCode;
    
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "max_output_tokens")
    private Integer maxOutputTokens;
    
    @Column(name = "top_p", precision = 3, scale = 2)
    private BigDecimal topP;
    
    @Column(name = "frequency_penalty", precision = 3, scale = 2)
    private BigDecimal frequencyPenalty;
    
    @Column(name = "presence_penalty", precision = 3, scale = 2)
    private BigDecimal presencePenalty;
    
    @Column(name = "context_window")
    private Integer contextWindow;
    
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    @Column(name = "api_version", length = 20)
    private String apiVersion;
    
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
    
    public String getModelCode() {
        return modelCode;
    }
    
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxOutputTokens() {
        return maxOutputTokens;
    }
    
    public void setMaxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
    
    public BigDecimal getTopP() {
        return topP;
    }
    
    public void setTopP(BigDecimal topP) {
        this.topP = topP;
    }
    
    public BigDecimal getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    public void setFrequencyPenalty(BigDecimal frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    
    public BigDecimal getPresencePenalty() {
        return presencePenalty;
    }
    
    public void setPresencePenalty(BigDecimal presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    
    public Integer getContextWindow() {
        return contextWindow;
    }
    
    public void setContextWindow(Integer contextWindow) {
        this.contextWindow = contextWindow;
    }
    
    public String getApiEndpoint() {
        return apiEndpoint;
    }
    
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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
}