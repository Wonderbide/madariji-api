package com.backcover.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class GcpConfig {

    private static final Logger log = LoggerFactory.getLogger(GcpConfig.class);


    @Bean
    public Storage googleCloudStorage() throws IOException {
        log.info("[STARTUP] GcpConfig: Creating Google Cloud Storage Bean - START");
        try {
            GoogleCredentials credentials = getGoogleCredentials();
            log.info("Google Cloud Storage credentials loaded successfully");
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            log.info("[STARTUP] GcpConfig: Google Cloud Storage Bean - SUCCESS");
            return storage;
        } catch (IOException e) {
            log.warn("=== GcpConfig: Google Cloud Storage Bean Creation Failed ===");
            log.warn("Google Cloud Storage credentials not found. GCS functionality will be disabled. Error: {}", e.getMessage());
            return null;
        }
    }

    private GoogleCredentials getGoogleCredentials() throws IOException {
        log.debug("=== GcpConfig: Loading Google Credentials ===");
        // Essayer d'abord la variable d'environnement GCP_CREDENTIALS_JSON (pour Doppler)
        String gcpCredentialsJson = System.getenv("GCP_CREDENTIALS_JSON");
        if (gcpCredentialsJson != null && !gcpCredentialsJson.isEmpty()) {
            log.info("GCP_CREDENTIALS_JSON environment variable found");
            log.debug("Credentials JSON length: {} characters", gcpCredentialsJson.length());
            
            // The JSON from environment variables has escaped newlines that need to be unescaped
            String unescapedJson = gcpCredentialsJson
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
            
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new java.io.ByteArrayInputStream(unescapedJson.getBytes())
            );
            log.info("=== GcpConfig: Successfully loaded GCP credentials from environment variable ===");
            return credentials;
        }
        
        // Sinon utiliser les credentials par défaut (fichier local)
        log.info("GCP_CREDENTIALS_JSON not found, attempting to use default application credentials");
        log.debug("GOOGLE_APPLICATION_CREDENTIALS env var: {}", 
            System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? "[SET]" : "[NOT SET]");
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        log.info("=== GcpConfig: Successfully loaded default application credentials ===");
        return credentials;
    }
}