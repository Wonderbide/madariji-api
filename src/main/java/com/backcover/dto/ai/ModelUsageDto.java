package com.backcover.dto.ai;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO showing model usage across flows
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelUsageDto {
    
    private String modelId;
    private String modelName;
    private String provider;
    private Boolean isEnabled;
    private Boolean isInUse;
    private List<String> usedInFlows;
    private String performanceTier;
    private Double costPer1kInputTokens;
    private Double costPer1kOutputTokens;
    private Integer maxContextTokens;
}