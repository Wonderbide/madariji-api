package com.backcover.service.tts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Mock TTS Service for development/testing without Google Cloud TTS
 */
@Slf4j
@Service
@Profile("mock-tts")
public class MockTTSService extends TTSService {

    public MockTTSService(TTSCacheService ttsCacheService) {
        super(null, ttsCacheService); // No real TTS client needed, but still use cache
    }

    @Override
    public byte[] synthesizeSpeech(String text) throws IOException {
        log.info("MOCK TTS: Generating fake audio for text: {}", text);
        
        // Return a minimal valid MP3 file (silent)
        // This is a tiny valid MP3 file with silence
        byte[] silentMp3 = new byte[] {
            (byte)0xFF, (byte)0xFB, (byte)0x90, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return silentMp3;
    }

    @Override
    public void init() {
        log.info("[STARTUP] MockTTSService - No initialization needed");
    }

    @Override
    public void clearCache() {
        log.info("MOCK TTS: Cache cleared (no-op)");
    }
}