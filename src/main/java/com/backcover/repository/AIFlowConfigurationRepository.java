package com.backcover.repository;

import com.backcover.model.AIFlowConfiguration;
import com.backcover.model.AIFlowConfiguration.FlowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AI Flow Configuration management
 * Updated to match new schema without provider and priority columns
 */
@Repository
public interface AIFlowConfigurationRepository extends JpaRepository<AIFlowConfiguration, UUID> {
    
    /**
     * Find active configuration for a specific workflow type
     */
    @Query("SELECT afc FROM AIFlowConfiguration afc WHERE afc.workflowType = :workflowType AND afc.isActive = true")
    Optional<AIFlowConfiguration> findActiveConfigByWorkflowType(@Param("workflowType") String workflowType);
    
    /**
     * Find all active configurations
     */
    List<AIFlowConfiguration> findByIsActiveTrue();
    
    /**
     * Find all configurations for a specific model
     */
    List<AIFlowConfiguration> findByModelId(String modelId);
    
    /**
     * Check if a model is actively used in any workflow
     */
    @Query("SELECT COUNT(afc) > 0 FROM AIFlowConfiguration afc WHERE afc.modelId = :modelId AND afc.isActive = true")
    boolean isModelActivelyUsed(@Param("modelId") String modelId);
    
    /**
     * Find all configurations for a workflow type
     */
    List<AIFlowConfiguration> findByWorkflowType(String workflowType);
}