package com.backcover.config;

import com.backcover.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/**
 * DISABLED - All prompts are now managed dynamically via database only.
 * Following NO FALLBACK policy: prompts must exist in database.
 * 
 * This class has been kept but disabled to maintain the configuration structure.
 * All hardcoded prompts have been removed and replaced with database management.
 */
@Configuration
@Profile("!test") // Don't run during tests
public class PromptDataInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(PromptDataInitializer.class);
    
    @Bean
    @Transactional
    CommandLineRunner initializePrompts(PromptTemplateRepository promptRepository) {
        return args -> {
            log.info("=== PromptDataInitializer: DISABLED - All prompts are managed via database ===");
            log.info("=== NO FALLBACK POLICY: Prompts must exist in database or system will fail ===");
            // All hardcoded prompt initialization has been removed
            // Prompts are now managed through database migrations and the prompt management system
        };
    }
}