package com.backcover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableAsync
@EnableScheduling
public class BackcoverApplication {

	private static final Logger log = LoggerFactory.getLogger(BackcoverApplication.class);

	public static void main(String[] args) {
		log.info("=== BACKCOVER APPLICATION STARTUP INITIATED ===");
		log.info("Java Version: {}", System.getProperty("java.version"));
		log.info("JVM Vendor: {}", System.getProperty("java.vendor"));
		log.info("OS: {} {} {}", System.getProperty("os.name"), 
				System.getProperty("os.version"), System.getProperty("os.arch"));
		log.info("Working Directory: {}", System.getProperty("user.dir"));
		log.info("User Home: {}", System.getProperty("user.home"));
		
		// Log key environment variables (without sensitive data)
		log.info("Environment - NODE_ENV: {}", System.getenv("NODE_ENV"));
		log.info("Environment - SPRING_PROFILES_ACTIVE: {}", System.getenv("SPRING_PROFILES_ACTIVE"));
		log.info("Environment - PORT: {}", System.getenv("PORT"));
		log.info("Environment - DATABASE_URL: {}", System.getenv("DATABASE_URL") != null ? "[SET]" : "[NOT SET]");
		log.info("Environment - GCP_CREDENTIALS_JSON: {}", System.getenv("GCP_CREDENTIALS_JSON") != null ? "[SET]" : "[NOT SET]");
		
		SpringApplication app = new SpringApplication(BackcoverApplication.class);
		
		// Add listeners for detailed startup tracking
		app.addListeners(
			(ApplicationListener<ApplicationStartingEvent>) event -> 
				log.info(">>> Application Starting Event - Loggers being initialized"),
			
			(ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> 
				log.info(">>> Application Environment Prepared - Active profiles: {}", 
					String.join(", ", event.getEnvironment().getActiveProfiles())),
			
			(ApplicationListener<ApplicationPreparedEvent>) event -> 
				log.info(">>> Application Context Prepared - Bean definitions loaded"),
			
			(ApplicationListener<ApplicationStartedEvent>) event -> 
				log.info(">>> Application Started - Context refreshed, runners not yet called"),
			
			(ApplicationListener<ApplicationReadyEvent>) event -> 
				log.info(">>> Application Ready - All startup tasks completed successfully!"),
			
			(ApplicationListener<ApplicationFailedEvent>) event -> 
				log.error(">>> Application Failed to Start!", event.getException())
		);
		
		log.info("Starting Spring Boot application...");
		app.run(args);
	}

}
