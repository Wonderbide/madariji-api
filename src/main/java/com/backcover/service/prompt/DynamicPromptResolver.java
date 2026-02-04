package com.backcover.service.prompt;

import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for resolving prompts dynamically from the database.
 * NO FALLBACK POLICY: If database is unavailable, the service will fail.
 */
@Service
public class DynamicPromptResolver {

    private static final Logger log = LoggerFactory.getLogger(DynamicPromptResolver.class);

    private final PromptTemplateRepository promptTemplateRepository;

    public DynamicPromptResolver(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }
    
    /**
     * Resolve a prompt template by identifier
     * @param identifier The prompt identifier (e.g., "WORD_ANALYSIS_V1")
     * @return The prompt template
     * @throws PromptNotFoundException if prompt not found
     */
    @Cacheable(value = "promptTemplates", key = "#identifier")
    public PromptTemplate resolvePrompt(String identifier) {
        log.debug("Resolving prompt template for identifier: {}", identifier);
        
        Optional<PromptTemplate> template = promptTemplateRepository
                .findByIdentifierAndIsActiveTrue(identifier);
        
        if (template.isPresent()) {
            log.debug("Found active prompt template: {} v{}", 
                    identifier, template.get().getVersion());
            return template.get();
        }
        
        log.error("No active prompt template found for identifier: {}", identifier);
        throw new PromptNotFoundException("No active prompt template found for: " + identifier);
    }
    
    /**
     * Resolve a prompt template by identifier and language
     * @param identifier The prompt identifier
     * @param languageCode The target language code (not used anymore - targetLanguageCode removed from DB)
     * @return The prompt template
     */
    @Cacheable(value = "promptTemplates", key = "#identifier + '_' + #languageCode")
    public PromptTemplate resolvePromptForLanguage(String identifier, String languageCode) {
        log.debug("Resolving prompt template for identifier: {} (language parameter ignored - not in DB)", 
                identifier);
        
        // targetLanguageCode removed from database schema - just return the active prompt
        return resolvePrompt(identifier);
    }
    
    /**
     * Build a prompt with the given parameters
     * @param template The prompt template
     * @param parameters The parameters to fill in the template
     * @return The built prompt string
     */
    public String buildPrompt(PromptTemplate template, Object... parameters) {
        try {
            String prompt = String.format(template.getPromptContent(), parameters);
            log.debug("Built prompt from template: {} with {} parameters", 
                    template.getIdentifier(), parameters.length);
            return prompt;
        } catch (Exception e) {
            log.error("Error building prompt from template {}: {}", 
                    template.getIdentifier(), e.getMessage());
            throw new PromptBuildException("Failed to build prompt: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get prompt content directly (for migration purposes)
     * @param identifier The prompt identifier
     * @return The prompt content string
     */
    public String getPromptContent(String identifier) {
        PromptTemplate template = resolvePrompt(identifier);
        return template.getPromptContent();
    }
    
    /**
     * Check if a prompt exists and is active
     * @param identifier The prompt identifier
     * @return true if prompt exists and is active
     */
    public boolean promptExists(String identifier) {
        return promptTemplateRepository
                .findByIdentifierAndIsActiveTrue(identifier)
                .isPresent();
    }
    
    /**
     * Custom exceptions
     */
    public static class PromptNotFoundException extends RuntimeException {
        public PromptNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class PromptBuildException extends RuntimeException {
        public PromptBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}