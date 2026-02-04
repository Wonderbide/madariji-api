package com.backcover.controller;

import com.backcover.dto.ai.AIFlowConfigurationDto;
import com.backcover.dto.ai.SetModelRequest;
import com.backcover.dto.ai.ModelUsageDto;
import com.backcover.model.AIFlowConfiguration;
import com.backcover.model.AIFlowConfiguration.FlowType;
import com.backcover.model.prompt.ModelConfiguration;
import com.backcover.service.AIModelConfigurationService;
import com.backcover.repository.ModelConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for AI model configuration management
 */
@RestController
@RequestMapping("/api/ai-configuration")
@CrossOrigin(origins = "*")
public class AIConfigurationController {
    
    private static final Logger log = LoggerFactory.getLogger(AIConfigurationController.class);
    
    private final AIModelConfigurationService aiConfigService;
    private final ModelConfigurationRepository modelConfigRepository;
    
    public AIConfigurationController(AIModelConfigurationService aiConfigService,
                                   ModelConfigurationRepository modelConfigRepository) {
        this.aiConfigService = aiConfigService;
        this.modelConfigRepository = modelConfigRepository;
    }
    
    /**
     * Get all active AI flow configurations
     */
    @GetMapping("/flows")
    public ResponseEntity<List<AIFlowConfigurationDto>> getAllFlowConfigurations() {
        log.info("Getting all AI flow configurations");
        
        List<AIFlowConfiguration> configs = aiConfigService.getAllActiveConfigurations();
        List<AIFlowConfigurationDto> dtos = configs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get active configurations for a specific flow type
     */
    @GetMapping("/flows/{flowType}")
    public ResponseEntity<List<AIFlowConfigurationDto>> getConfigurationsForFlow(
            @PathVariable FlowType flowType) {
        log.info("Getting configurations for flow type: {}", flowType);
        
        List<AIFlowConfiguration> configs = aiConfigService.getActiveConfigurationsForFlow(flowType);
        List<AIFlowConfigurationDto> dtos = configs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Set active model for a specific flow and provider
     */
    @PostMapping("/flows/set-model")
    public ResponseEntity<String> setActiveModel(@Valid @RequestBody SetModelRequest request) {
        log.info("Setting active model: {} for flow: {}", 
                request.getModelId(), request.getFlowType());
        
        try {
            aiConfigService.setActiveModel(request.getFlowType(), request.getModelId());
            return ResponseEntity.ok("Model configuration updated successfully");
        } catch (Exception e) {
            log.error("Error setting active model: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating model configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get model usage overview
     */
    @GetMapping("/models/usage")
    public ResponseEntity<List<ModelUsageDto>> getModelUsageOverview() {
        log.info("Getting model usage overview");
        
        List<ModelConfiguration> allModels = modelConfigRepository.findAll();
        List<ModelUsageDto> usageDtos = allModels.stream()
                .map(this::convertToUsageDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usageDtos);
    }
    
    /**
     * Get available flow types
     */
    @GetMapping("/flow-types")
    public ResponseEntity<Map<String, String>> getFlowTypes() {
        Map<String, String> flowTypes = Map.of(
            "WORD_ANALYSIS", "Word Analysis",
            "PAGE_STRUCTURING", "Page Structuring"
        );
        return ResponseEntity.ok(flowTypes);
    }
    
    /**
     * Get available providers
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, String>> getProviders() {
        Map<String, String> providers = Map.of(
            "GOOGLE", "Google (Gemini)"
        );
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get active model for specific flow and provider
     */
    @GetMapping("/flows/{flowType}/providers/{provider}/active-model")
    public ResponseEntity<String> getActiveModel(
            @PathVariable FlowType flowType,
            @PathVariable String provider) {
        
        return aiConfigService.getActiveModelForFlow(flowType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Helper methods
    private AIFlowConfigurationDto convertToDto(AIFlowConfiguration config) {
        AIFlowConfigurationDto dto = new AIFlowConfigurationDto();
        dto.setId(config.getId());
        dto.setFlowType(config.getFlowType());
        // Provider now comes from model configuration
        dto.setModelId(config.getModelId());
        dto.setIsActive(config.getIsActive());
        // Priority removed from database schema
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        dto.setCreatedBy(config.getConfiguredBy());
        
        // Add display names
        dto.setFlowTypeDisplayName(config.getFlowType().getDisplayName());
        // Provider display name removed
        
        // Get model name from model configuration
        modelConfigRepository.findByModelCode(config.getModelId())
                .ifPresent(modelConfig -> dto.setModelName(modelConfig.getModelName()));
        
        return dto;
    }
    
    private ModelUsageDto convertToUsageDto(ModelConfiguration model) {
        ModelUsageDto dto = new ModelUsageDto();
        dto.setModelId(model.getModelCode());
        dto.setModelName(model.getModelName());
        // Provider now comes from model configuration
        dto.setIsEnabled(true); // is_enabled column removed, assume all models are enabled
        dto.setIsInUse(aiConfigService.isModelInUse(model.getModelCode()));
        dto.setPerformanceTier(null); // performance_tier column removed
        dto.setCostPer1kInputTokens(0.0); // cost columns removed
        dto.setCostPer1kOutputTokens(0.0); // cost columns removed
        dto.setMaxContextTokens(model.getContextWindow());
        
        // Get flows where this model is used
        List<AIFlowConfiguration> configs = aiConfigService.getAllActiveConfigurations()
                .stream()
                .filter(config -> config.getModelId().equals(model.getId()))
                .collect(Collectors.toList());
        
        List<String> usedInFlows = configs.stream()
                .map(config -> config.getWorkflowType())
                .distinct()
                .collect(Collectors.toList());
        
        dto.setUsedInFlows(usedInFlows);
        
        return dto;
    }
}