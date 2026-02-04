package com.backcover.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "production")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        log.info("=== DatabaseConfig DataSource Creation Started (Production Profile) ===");
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            log.info("DATABASE_URL detected with postgresql:// format");
            // Parse Railway DATABASE_URL format
            URI dbUri = new URI(databaseUrl);
            
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            
            log.info("Parsed database connection details:");
            log.info("  - Host: {}", dbUri.getHost());
            log.info("  - Port: {}", dbUri.getPort());
            log.info("  - Database: {}", dbUri.getPath());
            log.info("  - Username: {}", username);
            log.info("  - JDBC URL: {}", dbUrl);
            
            DataSource dataSource = DataSourceBuilder.create()
                    .url(dbUrl)
                    .username(username)
                    .password(password)
                    .build();
                    
            log.info("=== DatabaseConfig DataSource Created Successfully (Railway format) ===");
            return dataSource;
        }
        
        // Fallback to standard configuration
        log.info("Using standard database configuration with individual environment variables");
        log.info("  - DB_URL: {}", System.getenv("DB_URL") != null ? "[SET]" : "[NOT SET]");
        log.info("  - DB_USERNAME: {}", System.getenv("DB_USERNAME") != null ? "[SET]" : "[NOT SET]");
        log.info("  - DB_PASSWORD: {}", System.getenv("DB_PASSWORD") != null ? "[SET]" : "[NOT SET]");
        
        DataSource dataSource = DataSourceBuilder.create()
                .url(System.getenv("DB_URL"))
                .username(System.getenv("DB_USERNAME"))
                .password(System.getenv("DB_PASSWORD"))
                .build();
                
        log.info("=== DatabaseConfig DataSource Created Successfully (Standard format) ===");
        return dataSource;
    }
}