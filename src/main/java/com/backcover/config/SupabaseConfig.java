package com.backcover.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

/**
 * Configuration for Supabase integration.
 * This configuration handles all Supabase-related settings.
 */
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SupabaseConfig.class);
    
    private String url;
    private String anonKey;
    private String jwtSecret;
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getAnonKey() {
        return anonKey;
    }
    
    public void setAnonKey(String anonKey) {
        this.anonKey = anonKey;
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    /**
     * Check if Supabase configuration is available
     */
    public boolean isConfigured() {
        return url != null && !url.isEmpty() 
            && anonKey != null && !anonKey.isEmpty() 
            && jwtSecret != null && !jwtSecret.isEmpty();
    }
    
    @PostConstruct
    public void logConfiguration() {
        log.info("=== SUPABASE CONFIGURATION ===");
        log.info("URL configured: {}", url != null && !url.isEmpty());
        log.info("Anon Key configured: {}", anonKey != null && !anonKey.isEmpty());
        log.info("JWT Secret configured: {}", jwtSecret != null && !jwtSecret.isEmpty());
        log.info("Is fully configured: {}", isConfigured());
        if (isConfigured()) {
            log.info("Supabase URL: {}", url);
        }
        log.info("==============================");
    }
}