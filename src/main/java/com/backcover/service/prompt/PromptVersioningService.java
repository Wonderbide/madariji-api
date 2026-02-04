package com.backcover.service.prompt;

import com.backcover.dto.prompt.*;
import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing prompt versioning
 */
@Service
public class PromptVersioningService {
    
    private static final Logger log = LoggerFactory.getLogger(PromptVersioningService.class);
    
    private final PromptTemplateRepository promptTemplateRepository;
    
    public PromptVersioningService(
            PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }
    
    /**
     * Create a new version of an existing prompt
     */
    @Transactional
    public PromptVersionDto createNewVersion(String identifier, CreatePromptVersionDto createDto, String createdBy) {
        log.info("Creating new version {} for prompt {}", createDto.getVersion(), identifier);
        
        // Check if version already exists
        List<PromptTemplate> existingVersions = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        boolean versionExists = existingVersions.stream()
                .anyMatch(template -> template.getVersion().equals(createDto.getVersion()));
        
        if (versionExists) {
            throw new IllegalArgumentException("Version " + createDto.getVersion() + " already exists for prompt " + identifier);
        }
        
        // Get the previous active version for reference
        Optional<PromptTemplate> previousActiveVersion = promptTemplateRepository.findByIdentifierAndIsActiveTrue(identifier);
        
        // Create new version
        PromptTemplate newVersion = new PromptTemplate();
        newVersion.setIdentifier(identifier);
        newVersion.setVersion(createDto.getVersion());
        newVersion.setPromptContent(createDto.getPromptContent());
        // category and createdBy columns removed from database schema
        newVersion.setIsActive(createDto.isMakeActive());
        
        // Copy values from previous version if not provided
        if (previousActiveVersion.isPresent()) {
            PromptTemplate prev = previousActiveVersion.get();
            // category column removed from database schema
            // targetLanguageCode removed from database schema
        }
        
        // If making this version active, deactivate previous versions
        if (createDto.isMakeActive()) {
            existingVersions.forEach(template -> template.setIsActive(false));
            promptTemplateRepository.saveAll(existingVersions);
        }
        
        // If deprecating previous version
        if (createDto.isDeprecatePrevious() && previousActiveVersion.isPresent()) {
            PromptTemplate prev = previousActiveVersion.get();
            // isDeprecated, deprecatedAt, deprecatedBy columns removed from database schema
            prev.setIsActive(false);
            promptTemplateRepository.save(prev);
        }
        
        PromptTemplate savedVersion = promptTemplateRepository.save(newVersion);
        
        log.info("Created new version {} for prompt {} by user {}", 
                savedVersion.getVersion(), identifier, createdBy);
        
        return convertToVersionDto(savedVersion);
    }
    
    /**
     * Get all versions of a prompt
     */
    public List<PromptVersionDto> getPromptVersions(String identifier) {
        List<PromptTemplate> versions = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        
        return versions.stream()
                .map(this::convertToVersionDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific version of a prompt
     */
    public Optional<PromptVersionDto> getPromptVersion(String identifier, String version) {
        List<PromptTemplate> templates = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        
        return templates.stream()
                .filter(template -> template.getVersion().equals(version))
                .findFirst()
                .map(this::convertToVersionDto);
    }
    
    /**
     * Activate a specific version
     */
    @Transactional
    public void activateVersion(String identifier, String version, String activatedBy) {
        log.info("Activating version {} for prompt {} by user {}", version, identifier, activatedBy);
        
        List<PromptTemplate> allVersions = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        
        // Find the version to activate
        PromptTemplate versionToActivate = allVersions.stream()
                .filter(template -> template.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version " + version + " not found for prompt " + identifier));
        
        // Deactivate all other versions
        allVersions.forEach(template -> template.setIsActive(false));
        
        // Activate the target version (updatedBy column removed)
        versionToActivate.setIsActive(true);
        
        promptTemplateRepository.saveAll(allVersions);
        
        log.info("Successfully activated version {} for prompt {}", version, identifier);
    }
    
    /**
     * Deprecate a specific version
     */
    @Transactional
    public void deprecateVersion(String identifier, String version, String deprecatedBy) {
        log.info("Deprecating version {} for prompt {} by user {}", version, identifier, deprecatedBy);
        
        List<PromptTemplate> templates = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        
        PromptTemplate versionToDeprecate = templates.stream()
                .filter(template -> template.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version " + version + " not found for prompt " + identifier));
        
        // Deprecation columns removed - just deactivate the version
        versionToDeprecate.setIsActive(false);
        versionToDeprecate.setIsActive(false);
        
        promptTemplateRepository.save(versionToDeprecate);
        
        log.info("Successfully deprecated version {} for prompt {}", version, identifier);
    }
    
    /**
     * Compare two versions of a prompt
     */
    public PromptVersionComparisonDto compareVersions(String identifier, String version1, String version2) {
        List<PromptTemplate> templates = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
        
        PromptTemplate v1 = templates.stream()
                .filter(template -> template.getVersion().equals(version1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version " + version1 + " not found"));
        
        PromptTemplate v2 = templates.stream()
                .filter(template -> template.getVersion().equals(version2))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Version " + version2 + " not found"));
        
        PromptVersionComparisonDto comparison = new PromptVersionComparisonDto();
        comparison.setVersion1(convertToVersionDto(v1));
        comparison.setVersion2(convertToVersionDto(v2));
        
        // Calculate differences
        comparison.setContentChanged(!v1.getPromptContent().equals(v2.getPromptContent()));
        comparison.setModelChanged(false); // Default model field removed
        comparison.setParametersChanged(false); // Parameters now in model configuration
        
        return comparison;
    }
    
    /**
     * Convert PromptTemplate to PromptVersionDto with metrics
     */
    private PromptVersionDto convertToVersionDto(PromptTemplate template) {
        PromptVersionDto dto = new PromptVersionDto();
        dto.setId(template.getId());
        dto.setIdentifier(template.getIdentifier());
        dto.setVersion(template.getVersion());
        dto.setActive(template.getIsActive());
        // Removed columns: deprecated, createdBy, deprecatedAt, deprecatedBy
        dto.setDeprecated(false);  // Always false since column removed
        dto.setCreatedAt(template.getCreatedAt());
        dto.setCreatedBy(null);  // Column removed
        dto.setDeprecatedAt(null);  // Column removed
        dto.setDeprecatedBy(null);  // Column removed
        
        // Metrics removed - no longer tracking usage
        dto.setMetrics(new PromptVersionDto.PromptVersionMetrics());
        
        return dto;
    }
    
}