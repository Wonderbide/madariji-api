package com.backcover.controller;

import com.backcover.dto.prompt.*;
import com.backcover.service.prompt.PromptVersioningService;
import com.backcover.util.security.AuthenticationHelper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for prompt versioning operations
 */
@RestController
@RequestMapping("/api/prompt-versioning")
public class PromptVersioningController {
    
    private static final Logger log = LoggerFactory.getLogger(PromptVersioningController.class);
    
    private final PromptVersioningService promptVersioningService;
    private final AuthenticationHelper authenticationHelper;
    
    public PromptVersioningController(
            PromptVersioningService promptVersioningService,
            AuthenticationHelper authenticationHelper) {
        this.promptVersioningService = promptVersioningService;
        this.authenticationHelper = authenticationHelper;
    }
    
    /**
     * Create a new version of a prompt
     */
    @PostMapping("/{identifier}")
    public ResponseEntity<PromptVersionDto> createNewVersion(
            @PathVariable String identifier,
            @Valid @RequestBody CreatePromptVersionDto createDto,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            String createdBy = authenticationHelper.getEmail(jwt);
            
            log.info("User {} creating new version {} for prompt {}", 
                    user.getSupabaseUserId(), createDto.getVersion(), identifier);
            
            PromptVersionDto newVersion = promptVersioningService.createNewVersion(
                    identifier, createDto, createdBy);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(newVersion);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating prompt version: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating new prompt version", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all versions of a prompt
     */
    @GetMapping("/{identifier}/versions")
    public ResponseEntity<List<PromptVersionDto>> getPromptVersions(
            @PathVariable String identifier,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            log.info("User {} requested versions for prompt {}", user.getSupabaseUserId(), identifier);
            
            List<PromptVersionDto> versions = promptVersioningService.getPromptVersions(identifier);
            
            return ResponseEntity.ok(versions);
            
        } catch (Exception e) {
            log.error("Error retrieving prompt versions for identifier: {}", identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific version of a prompt
     */
    @GetMapping("/{identifier}/versions/{version}")
    public ResponseEntity<PromptVersionDto> getPromptVersion(
            @PathVariable String identifier,
            @PathVariable String version,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            log.info("User {} requested version {} for prompt {}", 
                    user.getSupabaseUserId(), version, identifier);
            
            Optional<PromptVersionDto> versionDto = promptVersioningService.getPromptVersion(identifier, version);
            
            if (versionDto.isPresent()) {
                return ResponseEntity.ok(versionDto.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error retrieving prompt version {} for identifier: {}", version, identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Activate a specific version
     */
    @PostMapping("/{identifier}/versions/{version}/activate")
    public ResponseEntity<Void> activateVersion(
            @PathVariable String identifier,
            @PathVariable String version,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            String activatedBy = authenticationHelper.getEmail(jwt);
            
            log.info("User {} activating version {} for prompt {}", 
                    user.getSupabaseUserId(), version, identifier);
            
            promptVersioningService.activateVersion(identifier, version, activatedBy);
            
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for activating prompt version: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error activating prompt version {} for identifier: {}", version, identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deprecate a specific version
     */
    @PostMapping("/{identifier}/versions/{version}/deprecate")
    public ResponseEntity<Void> deprecateVersion(
            @PathVariable String identifier,
            @PathVariable String version,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            String deprecatedBy = authenticationHelper.getEmail(jwt);
            
            log.info("User {} deprecating version {} for prompt {}", 
                    user.getSupabaseUserId(), version, identifier);
            
            promptVersioningService.deprecateVersion(identifier, version, deprecatedBy);
            
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for deprecating prompt version: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deprecating prompt version {} for identifier: {}", version, identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Compare two versions of a prompt
     */
    @GetMapping("/{identifier}/versions/{version1}/compare/{version2}")
    public ResponseEntity<PromptVersionComparisonDto> compareVersions(
            @PathVariable String identifier,
            @PathVariable String version1,
            @PathVariable String version2,
            @AuthenticationPrincipal Jwt jwt) {
        
        try {
            var user = authenticationHelper.getRequiredAuthenticatedUser(jwt);
            
            log.info("User {} comparing versions {} vs {} for prompt {}", 
                    user.getSupabaseUserId(), version1, version2, identifier);
            
            PromptVersionComparisonDto comparison = promptVersioningService.compareVersions(
                    identifier, version1, version2);
            
            return ResponseEntity.ok(comparison);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for comparing prompt versions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error comparing prompt versions {} vs {} for identifier: {}", 
                    version1, version2, identifier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}