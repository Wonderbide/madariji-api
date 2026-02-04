package com.backcover.dto.ai;

import com.backcover.model.AIFlowConfiguration.FlowType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for setting active model for a flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetModelRequest {
    
    @NotNull(message = "Flow type is required")
    private FlowType flowType;
    
    @NotBlank(message = "Model ID is required")
    private String modelId;
}