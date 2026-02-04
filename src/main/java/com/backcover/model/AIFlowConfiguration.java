package com.backcover.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuration entity for AI workflow model assignment.
 * Matches AI_CONFIG_MINIMAL.md specification exactly
 */
@Entity
@Table(name = "ai_workflow_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIFlowConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "workflow_type", nullable = false)
    private String workflowType;
    
    @Column(name = "prompt_id")
    private UUID promptId;
    
    @Column(name = "model_id", nullable = false)
    private String modelId;  // Stores model UUID as string for now
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "configured_by")
    private String configuredBy;
    
    // Keep the enums for the workflow types only
    public enum FlowType {
        WORD_ANALYSIS("Word Analysis"),
        PAGE_STRUCTURING("Page Structuring");
        
        private final String displayName;
        
        FlowType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Temporary compatibility method for code that still uses FlowType enum
    public FlowType getFlowType() {
        try {
            return FlowType.valueOf(workflowType);
        } catch (Exception e) {
            return null;
        }
    }
    
    public void setFlowType(FlowType flowType) {
        this.workflowType = flowType != null ? flowType.name() : null;
    }
}