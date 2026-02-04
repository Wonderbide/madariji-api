package com.backcover.controller;

import com.backcover.dto.prompt.*;
import com.backcover.model.User;
import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.PromptTemplateRepository;
import com.backcover.util.security.AuthenticationHelper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for managing prompt templates
 */
@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {
    
    private static final Logger log = LoggerFactory.getLogger(PromptTemplateController.class);
    
    private final PromptTemplateRepository promptTemplateRepository;
    private final AuthenticationHelper authenticationHelper;
    
    public PromptTemplateController(
            PromptTemplateRepository promptTemplateRepository,
            AuthenticationHelper authenticationHelper) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Get all prompt templates with optional filtering
     * Admin access required
     */
    @GetMapping
    public ResponseEntity<List<PromptTemplateDto>> getPromptTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String languageCode,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Require admin access
        authenticationHelper.requireAdmin(jwt);
        
        try {
            List<PromptTemplate> templates;
            
            if (category != null) {
                // targetLanguageCode removed from database schema - can't filter by language
                templates = promptTemplateRepository.findByCategoryAndIsActiveTrueOrderByVersionDesc(category);
            } else if (activeOnly) {
                templates = promptTemplateRepository.findByIsActiveTrueOrderByCategory();
            } else {
                templates = promptTemplateRepository.findAll();
            }
            
            List<PromptTemplateDto> templateDtos = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(templateDtos);
            
        } catch (Exception e) {
            log.error("Error retrieving prompt templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific prompt template by ID
     * Admin access required
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplateDto> getPromptTemplate(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Require admin access
        authenticationHelper.requireAdmin(jwt);
        
        try {
            PromptTemplate template = promptTemplateRepository.findById(id).orElse(null);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(convertToDto(template));
            
        } catch (Exception e) {
            log.error("Error retrieving prompt template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get prompt template by identifier (returns active version)
     * Admin access required
     */
    @GetMapping("/by-identifier/{identifier}")
    public ResponseEntity<PromptTemplateDto> getPromptTemplateByIdentifier(
            @PathVariable String identifier,
            @RequestParam(required = false) String languageCode,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Require admin access
        authenticationHelper.requireAdmin(jwt);
        
        try {
            PromptTemplate template;
            
            if (languageCode != null) {
                // targetLanguageCode removed from database schema - can't filter by language code
                template = promptTemplateRepository
                    .findByIdentifierAndIsActiveTrue(identifier)
                    .orElse(null);
            } else {
                template = promptTemplateRepository
                    .findByIdentifierAndIsActiveTrue(identifier)
                    .orElse(null);
            }
            
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(convertToDto(template));
            
        } catch (Exception e) {
            log.error("Error retrieving prompt template by identifier: {}", identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all versions of a prompt by identifier
     * Admin access required
     */
    @GetMapping("/versions/{identifier}")
    public ResponseEntity<List<PromptTemplateDto>> getPromptTemplateVersions(
            @PathVariable String identifier,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Require admin access
        authenticationHelper.requireAdmin(jwt);
        
        try {
            List<PromptTemplate> templates = promptTemplateRepository.findByIdentifierOrderByVersionDesc(identifier);
            
            List<PromptTemplateDto> templateDtos = templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(templateDtos);
            
        } catch (Exception e) {
            log.error("Error retrieving prompt template versions for: {}", identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new prompt template (admin only)
     */
    @PostMapping
    public ResponseEntity<PromptTemplateDto> createPromptTemplate(
            @Valid @RequestBody CreatePromptTemplateDto createDto,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Require admin access
            authenticationHelper.requireAdmin(jwt);
            User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            // Check if identifier already exists
            if (promptTemplateRepository.existsByIdentifier(createDto.getIdentifier())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            PromptTemplate template = new PromptTemplate();
            template.setIdentifier(createDto.getIdentifier());
            template.setPromptContent(createDto.getPromptContent());
            template.setVersion(createDto.getVersion());
            // category and createdBy columns removed from database schema
            template.setIsActive(true);
            
            PromptTemplate savedTemplate = promptTemplateRepository.save(template);
            
            log.info("Prompt template created: {} by user: {}", 
                savedTemplate.getId(), authenticationHelper.getEmail(jwt));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedTemplate));
            
        } catch (Exception e) {
            log.error("Error creating prompt template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing prompt template (admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplateDto> updatePromptTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePromptTemplateDto updateDto,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            // Require admin access
            authenticationHelper.requireAdmin(jwt);
            User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            PromptTemplate template = promptTemplateRepository.findById(id).orElse(null);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields if provided
            
            if (updateDto.getPromptContent() != null) {
                template.setPromptContent(updateDto.getPromptContent());
            }
            
            // category column removed from database schema
            
            // targetLanguageCode removed from database schema
            
            
            
            if (updateDto.getIsActive() != null) {
                template.setIsActive(updateDto.getIsActive());
            }
            
            // updatedBy column removed from database schema
            
            PromptTemplate savedTemplate = promptTemplateRepository.save(template);
            
            log.info("Prompt template updated: {} by user: {}", 
                savedTemplate.getId(), authenticationHelper.getEmail(jwt));
            
            return ResponseEntity.ok(convertToDto(savedTemplate));
            
        } catch (Exception e) {
            log.error("Error updating prompt template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a prompt template (admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromptTemplate(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Require admin access
            authenticationHelper.requireAdmin(jwt);
            User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            PromptTemplate template = promptTemplateRepository.findById(id).orElse(null);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            promptTemplateRepository.delete(template);
            
            log.info("Prompt template deleted: {} by user: {}", 
                id, authenticationHelper.getEmail(jwt));
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting prompt template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Activate a specific version of a prompt template (admin only)
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<PromptTemplateDto> activatePromptTemplate(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Require admin access
            authenticationHelper.requireAdmin(jwt);
            User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            PromptTemplate template = promptTemplateRepository.findById(id).orElse(null);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Deactivate all other versions of this prompt
            List<PromptTemplate> allVersions = promptTemplateRepository
                .findByIdentifierOrderByVersionDesc(template.getIdentifier());
            
            for (PromptTemplate version : allVersions) {
                version.setIsActive(false);
                // updatedBy column removed from database schema
            }
            promptTemplateRepository.saveAll(allVersions);
            
            // Activate the selected version
            template.setIsActive(true);
            // updatedBy column removed from database schema
            PromptTemplate savedTemplate = promptTemplateRepository.save(template);
            
            log.info("Prompt template activated: {} (identifier: {}) by user: {}", 
                savedTemplate.getId(), savedTemplate.getIdentifier(), 
                authenticationHelper.getEmail(jwt));
            
            return ResponseEntity.ok(convertToDto(savedTemplate));
            
        } catch (Exception e) {
            log.error("Error activating prompt template: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Convert PromptTemplate entity to DTO
     */
    private PromptTemplateDto convertToDto(PromptTemplate template) {
        PromptTemplateDto dto = new PromptTemplateDto();
        dto.setId(template.getId());
        dto.setIdentifier(template.getIdentifier());
        dto.setPromptContent(template.getPromptContent());
        dto.setVersion(template.getVersion());
        dto.setIsActive(template.getIsActive());
        // category, createdBy, updatedBy columns removed from database schema
        dto.setCategory(null);
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        dto.setCreatedBy(null);
        dto.setUpdatedBy(null);
        return dto;
    }
}