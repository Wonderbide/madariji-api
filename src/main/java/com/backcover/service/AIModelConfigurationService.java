package com.backcover.service;

import com.backcover.exception.ConfigurationNotFoundException;
import com.backcover.model.AIFlowConfiguration;
import com.backcover.model.AIFlowConfiguration.FlowType;
import com.backcover.repository.AIFlowConfigurationRepository;
import com.backcover.repository.ModelConfigurationRepository;
import com.backcover.model.prompt.ModelConfiguration;
import com.backcover.dto.ModelConfigurationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing dynamic AI model configuration per workflow type.
 * Allows switching between different models for different AI tasks.
 */
@Service
@Transactional(readOnly = true)
public class AIModelConfigurationService {
    
    private static final Logger log = LoggerFactory.getLogger(AIModelConfigurationService.class);
    
    private final AIFlowConfigurationRepository configurationRepository;
    private final ModelConfigurationRepository modelConfigurationRepository;
    
    public AIModelConfigurationService(AIFlowConfigurationRepository configurationRepository,
                                     ModelConfigurationRepository modelConfigurationRepository) {
        this.configurationRepository = configurationRepository;
        this.modelConfigurationRepository = modelConfigurationRepository;
    }
    
    /**
     * Get the active model ID for a specific flow type and provider
     */
    public Optional<String> getActiveModelForFlow(FlowType flowType) {
        log.info("🔍 AI MODEL CONFIG - Searching for active model | Flow: {}", flowType);
        
        String workflowType = flowType.name();
        Optional<AIFlowConfiguration> config = configurationRepository
                .findActiveConfigByWorkflowType(workflowType);
        
        if (config.isPresent()) {
            String modelId = config.get().getModelId();
            log.info("✅ AI MODEL CONFIG - Found active model: {} | Flow: {}", modelId, flowType);
            return Optional.of(modelId);
        }
        
        log.warn("❌ AI MODEL CONFIG - No active configuration found | Flow: {}", flowType);
        return Optional.empty();
    }
    
    /**
     * Get the primary active model for a flow type (regardless of provider)
     */
    public Optional<String> getPrimaryModelForFlow(FlowType flowType) {
        log.debug("Getting primary model for flow: {}", flowType);
        
        Optional<AIFlowConfiguration> config = configurationRepository
                .findActiveConfigByWorkflowType(flowType.name());
        
        if (config.isPresent()) {
            String modelId = config.get().getModelId();
            log.debug("Found primary model: {} for flow: {}", modelId, flowType);
            return Optional.of(modelId);
        }
        
        log.warn("No primary model configuration found for flow: {}", flowType);
        return Optional.empty();
    }
    
    /**
     * Get all active configurations for a flow type
     */
    public List<AIFlowConfiguration> getActiveConfigurationsForFlow(FlowType flowType) {
        return configurationRepository.findByWorkflowType(flowType.name()).stream()
            .filter(AIFlowConfiguration::getIsActive)
            .toList();
    }
    
    /**
     * Get the required active configuration for a flow type (throws exception if not found)
     * @throws ConfigurationNotFoundException if no active configuration is found
     */
    public AIFlowConfiguration getRequiredActiveConfiguration(FlowType flowType) {
        return getActiveConfigurationsForFlow(flowType)
            .stream()
            .findFirst()
            .orElseThrow(() -> new ConfigurationNotFoundException(
                String.format("No active configuration found for flow type: %s. Please configure an AI model for this flow.", flowType)
            ));
    }
    
    /**
     * Get the required active model for a flow type (throws exception if not found)
     * @throws ConfigurationNotFoundException if no active configuration is found
     */
    public String getRequiredActiveModelForFlow(FlowType flowType) {
        AIFlowConfiguration config = getRequiredActiveConfiguration(flowType);
        log.info("✅ AI MODEL CONFIG - Found active model: {} | Flow: {}", 
                config.getModelId(), flowType);
        return config.getModelId();
    }
    
    /**
     * Set active model for a specific flow and provider
     */
    @Transactional
    public void setActiveModel(FlowType flowType, String modelId) {
        log.info("Setting active model: {} for flow: {}", modelId, flowType);
        
        String workflowType = flowType.name();
        
        // Deactivate current active model for this flow
        Optional<AIFlowConfiguration> currentActive = configurationRepository
                .findActiveConfigByWorkflowType(workflowType);
        
        if (currentActive.isPresent()) {
            AIFlowConfiguration current = currentActive.get();
            current.setIsActive(false);
            configurationRepository.save(current);
            log.debug("Deactivated previous model: {} for flow: {}", 
                     current.getModelId(), flowType);
        }
        
        // Activate new model or create new configuration
        AIFlowConfiguration newConfig = new AIFlowConfiguration();
        newConfig.setFlowType(flowType);
        newConfig.setModelId(modelId);
        newConfig.setIsActive(true);
        
        configurationRepository.save(newConfig);
        log.info("Activated new model: {} for flow: {}", modelId, flowType);
    }
    
    /**
     * Check if a model is actively used in any workflow
     */
    public boolean isModelInUse(String modelId) {
        return configurationRepository.isModelActivelyUsed(modelId);
    }
    
    /**
     * Get all active AI configurations
     */
    public List<AIFlowConfiguration> getAllActiveConfigurations() {
        return configurationRepository.findByIsActiveTrue();
    }
    
    /**
     * Get model configuration with temperature and max tokens for a specific model
     */
    public ModelConfigurationDto getModelConfiguration(String modelId) {
        ModelConfiguration config = modelConfigurationRepository.findByModelCode(modelId)
            .orElseThrow(() -> new ConfigurationNotFoundException(
                String.format("No configuration found for model: %s", modelId)));
        
        // Use default temperature or the one specified in DB
        Double temperature = config.getTemperature() != null ? 
            config.getTemperature() : 0.05;
        
        // Use max output tokens from DB or default
        Integer maxTokens = config.getMaxOutputTokens() != null ?
            config.getMaxOutputTokens() : 4096;
            
        log.info("🎯 MODEL CONFIG - Retrieved for model: {} | Temperature: {} | MaxTokens: {}", 
                modelId, temperature, maxTokens);
                
        return new ModelConfigurationDto(modelId, temperature, maxTokens);
    }
}