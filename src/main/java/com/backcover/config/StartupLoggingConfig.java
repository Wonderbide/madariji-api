package com.backcover.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;

/**
 * Configuration class dedicated to comprehensive startup logging.
 * Logs various application lifecycle events and configuration details.
 */
@Configuration
public class StartupLoggingConfig {
    
    private static final Logger log = LoggerFactory.getLogger(StartupLoggingConfig.class);
    
    private final Environment environment;
    
    @Value("${spring.application.name:backcover}")
    private String applicationName;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfiles;
    
    @Value("${file.storage.base-dir-name:local}")
    private String storageBaseDir;
    
    public StartupLoggingConfig(Environment environment) {
        this.environment = environment;
        log.info("=== StartupLoggingConfig Constructor Called ===");
    }
    
    @PostConstruct
    public void logStartupConfiguration() {
        log.info("=================================================");
        log.info("=== APPLICATION CONFIGURATION SUMMARY ===");
        log.info("=================================================");
        log.info("Application Name: {}", applicationName);
        log.info("Server Port: {}", serverPort);
        log.info("Active Profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Default Profiles: {}", Arrays.toString(environment.getDefaultProfiles()));
        log.info("Storage Base Directory: {}", storageBaseDir);
        
        // Log datasource configuration (without passwords)
        log.info("--- Database Configuration ---");
        log.info("DataSource URL: {}", environment.getProperty("spring.datasource.url", "[NOT SET]"));
        log.info("DataSource Driver: {}", environment.getProperty("spring.datasource.driver-class-name", "[NOT SET]"));
        log.info("JPA Database Platform: {}", environment.getProperty("spring.jpa.database-platform", "[NOT SET]"));
        log.info("JPA Hibernate DDL Auto: {}", environment.getProperty("spring.jpa.hibernate.ddl-auto", "[NOT SET]"));
        
        // Log other important configurations
        log.info("--- File Storage Configuration ---");
        log.info("PDF Subdirectory: {}", environment.getProperty("file.storage.pdf-subdir-name", "[NOT SET]"));
        log.info("Results Subdirectory: {}", environment.getProperty("file.storage.final-results-subdir-name", "[NOT SET]"));
        log.info("Cover Subdirectory: {}", environment.getProperty("file.storage.cover-subdir-name", "[NOT SET]"));
        
        log.info("--- GCP Configuration ---");
        log.info("GCP Project ID: {}", environment.getProperty("gcp.project-id", "[NOT SET]"));
        log.info("GCP Storage Bucket: {}", environment.getProperty("gcp.storage.bucket-name", "[NOT SET]"));
        
        log.info("=================================================");
    }
    
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        log.info(">>> Spring Context Refreshed - All beans are initialized");
    }
    
    @EventListener
    public void handleContextStarted(ContextStartedEvent event) {
        log.info(">>> Spring Context Started");
    }
    
    @EventListener
    public void handleContextStopped(ContextStoppedEvent event) {
        log.info(">>> Spring Context Stopped");
    }
    
    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        log.info(">>> Spring Context Closed - Application shutting down");
    }
    
    @PreDestroy
    public void logShutdown() {
        log.info("=== StartupLoggingConfig @PreDestroy - Application is shutting down ===");
    }
}