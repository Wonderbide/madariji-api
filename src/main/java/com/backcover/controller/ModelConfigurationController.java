package com.backcover.controller;

import com.backcover.model.prompt.ModelConfiguration;
import com.backcover.model.AIFlowConfiguration;
import com.backcover.repository.ModelConfigurationRepository;
import com.backcover.service.AIModelConfigurationService;
import com.backcover.util.security.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for AI model configuration APIs.
 * This is a stub implementation for Sprint 2.
 */
@RestController
@RequestMapping("/api/models")
public class ModelConfigurationController {
    
    private static final Logger log = LoggerFactory.getLogger(ModelConfigurationController.class);
    
    private final ModelConfigurationRepository modelConfigurationRepository;
    private final AuthenticationHelper authenticationHelper;
    private final AIModelConfigurationService aiModelConfigurationService;
    
    public ModelConfigurationController(
            ModelConfigurationRepository modelConfigurationRepository,
            AuthenticationHelper authenticationHelper,
            AIModelConfigurationService aiModelConfigurationService) {
        this.modelConfigurationRepository = modelConfigurationRepository;
        this.authenticationHelper = authenticationHelper;
        this.aiModelConfigurationService = aiModelConfigurationService;
    }
    
    /**
     * Get all enabled model configurations
     */
    @GetMapping
    public ResponseEntity<List<ModelConfiguration>> getModels(
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("User {} requested model configurations", supabaseUserId);
        
        // Note: is_enabled column removed, returning all models
        List<ModelConfiguration> models = modelConfigurationRepository.findAll();
        return ResponseEntity.ok(models);
    }
    
    /**
     * Get all available LLM models with their configuration and active status
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableModels(
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("User {} requested available model configurations with active status", supabaseUserId);
        
        // Get all model configurations
        List<ModelConfiguration> allModels = modelConfigurationRepository.findAll();
        
        // Get active AI flow configurations
        List<AIFlowConfiguration> activeFlows = aiModelConfigurationService.getAllActiveConfigurations();
        
        // Create a map of modelId to active flows
        Map<String, List<Map<String, String>>> activeModelFlows = new HashMap<>();
        for (AIFlowConfiguration flow : activeFlows) {
            activeModelFlows.computeIfAbsent(flow.getModelId(), k -> new ArrayList<>())
                .add(Map.of(
                    "flowType", flow.getFlowType().name(),
                    "flowDisplayName", flow.getFlowType().getDisplayName()
                ));
        }
        
        // Build response with model details and active status
        List<Map<String, Object>> modelDetails = allModels.stream()
            .map(model -> {
                Map<String, Object> details = new HashMap<>();
                details.put("id", model.getId());
                details.put("modelId", model.getModelCode());
                details.put("modelName", model.getModelName());
                details.put("provider", model.getProvider());
                details.put("apiEndpoint", model.getApiEndpoint());
                details.put("contextWindow", model.getContextWindow());
                details.put("maxOutputTokens", model.getMaxOutputTokens());
                details.put("temperature", model.getTemperature());
                details.put("topP", model.getTopP());
                details.put("frequencyPenalty", model.getFrequencyPenalty());
                details.put("presencePenalty", model.getPresencePenalty());
                details.put("apiVersion", model.getApiVersion());
                
                // Add active flow information
                List<Map<String, String>> activeInFlows = activeModelFlows.get(model.getModelCode());
                details.put("isActive", activeInFlows != null && !activeInFlows.isEmpty());
                details.put("activeInFlows", activeInFlows != null ? activeInFlows : new ArrayList<>());
                
                return details;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("models", modelDetails);
        response.put("totalModels", allModels.size());
        response.put("enabledModels", allModels.size()); // is_enabled column removed
        response.put("activeModels", activeModelFlows.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get model configuration by ID
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<ModelConfiguration> getModel(
            @PathVariable String modelId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("User {} requested model configuration for: {}", supabaseUserId, modelId);
        
        return modelConfigurationRepository.findByModelCode(modelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get model comparison data (stub)
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getModelComparison(
            @RequestParam(required = false) String promptIdentifier,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("User {} requested model comparison for prompt: {}", supabaseUserId, promptIdentifier);
        
        // Stub response for now
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("promptIdentifier", promptIdentifier);
        comparison.put("models", new HashMap<>());
        comparison.put("message", "Model comparison API will be implemented in Sprint 2");
        
        return ResponseEntity.ok(comparison);
    }
    
    /**
     * Get performance metrics for a specific model (stub)
     */
    @GetMapping("/{modelId}/performance")
    public ResponseEntity<Map<String, Object>> getModelPerformance(
            @PathVariable String modelId,
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("User {} requested performance metrics for model: {} (last {} days)", 
                supabaseUserId, modelId, days);
        
        // Stub response for now
        Map<String, Object> performance = new HashMap<>();
        performance.put("modelId", modelId);
        performance.put("averageResponseTime", 0);
        performance.put("successRate", 100.0);
        performance.put("totalRequests", 0);
        performance.put("averageCost", 0.0);
        performance.put("period", days + " days");
        performance.put("message", "Performance metrics API will be implemented in Sprint 2");
        
        return ResponseEntity.ok(performance);
    }
}