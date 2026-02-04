package com.backcover.config;

import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Slf4j
@Configuration
public class TTSConfig {

    @Bean
    @Profile("!mock-tts")
    public TextToSpeechClient textToSpeechClient() throws IOException {
        log.info("Initializing Google Cloud Text-to-Speech client");
        try {
            // Use GCP_CREDENTIALS_JSON if available
            String gcpCredentialsJson = System.getenv("GCP_CREDENTIALS_JSON");
            if (gcpCredentialsJson != null && !gcpCredentialsJson.isEmpty()) {
                log.info("Using GCP_CREDENTIALS_JSON for TTS client");
                // The JSON from environment variables has escaped newlines that need to be unescaped
                String unescapedJson = gcpCredentialsJson
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new java.io.ByteArrayInputStream(unescapedJson.getBytes())
                );
                TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
                return TextToSpeechClient.create(settings);
            }
            
            // Fallback to default credentials
            log.info("Using default application credentials for TTS client");
            return TextToSpeechClient.create();
        } catch (Exception e) {
            log.error("Failed to create TextToSpeechClient. Make sure:");
            log.error("1. Cloud Text-to-Speech API is enabled in your GCP project");
            log.error("2. GOOGLE_APPLICATION_CREDENTIALS or GCP_CREDENTIALS_JSON is properly configured");
            log.error("3. The service account has the necessary permissions");
            throw e;
        }
    }
    
    @Bean
    @Profile("mock-tts")
    public TextToSpeechClient mockTextToSpeechClient() {
        log.info("Using MOCK Text-to-Speech client (no real TTS)");
        return null; // MockTTSService doesn't need a real client
    }
}