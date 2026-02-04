package com.backcover.dto.ai;

import com.backcover.model.AIFlowConfiguration.FlowType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for AI Flow Configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIFlowConfigurationDto {
    
    private UUID id;
    private FlowType flowType;
    private String modelId;
    private String modelName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Additional computed fields
    private String flowTypeDisplayName;
}