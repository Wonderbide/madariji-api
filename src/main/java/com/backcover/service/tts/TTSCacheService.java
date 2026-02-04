package com.backcover.service.tts;

import com.backcover.service.storage.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service de cache TTS utilisant R2
 * Remplace le cache local par un cache dans R2
 */
@Service
public class TTSCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(TTSCacheService.class);
    private static final String TTS_CACHE_PREFIX = "tts/cache/";
    
    private final R2StorageService r2StorageService;
    
    public TTSCacheService(@Qualifier("mainR2Storage") R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
    }
    
    /**
     * Récupère un fichier audio depuis le cache R2
     */
    public Optional<byte[]> getCachedAudio(String cacheKey) {
        try {
            String r2Key = TTS_CACHE_PREFIX + cacheKey + ".mp3";
            Optional<byte[]> audio = r2StorageService.downloadFile(r2Key);
            
            if (audio.isPresent()) {
                log.debug("[TTS-CACHE] Cache hit for key: {}", cacheKey);
            } else {
                log.debug("[TTS-CACHE] Cache miss for key: {}", cacheKey);
            }
            
            return audio;
            
        } catch (Exception e) {
            log.warn("[TTS-CACHE] Error reading cache for key {}: {}", cacheKey, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Sauvegarde un fichier audio dans le cache R2
     */
    public void cacheAudio(String cacheKey, byte[] audioData, String voiceName, String text) {
        try {
            String r2Key = TTS_CACHE_PREFIX + cacheKey + ".mp3";
            
            // Préparer les métadonnées
            Map<String, String> metadata = new HashMap<>();
            metadata.put("content-type", "audio/mpeg");
            metadata.put("cache-key", cacheKey);
            metadata.put("voice-name", voiceName);
            metadata.put("text-length", String.valueOf(text.length()));
            metadata.put("cached-at", String.valueOf(System.currentTimeMillis()));
            
            // Sauvegarder dans R2
            r2StorageService.uploadFile(r2Key, audioData, metadata);
            
            log.debug("[TTS-CACHE] Cached audio for key: {} ({} bytes)", cacheKey, audioData.length);
            
        } catch (Exception e) {
            log.warn("[TTS-CACHE] Error caching audio for key {}: {}", cacheKey, e.getMessage());
            // Ne pas propager l'erreur - le cache est optionnel
        }
    }
    
    /**
     * Vérifie si un fichier audio existe dans le cache
     */
    public boolean isCached(String cacheKey) {
        String r2Key = TTS_CACHE_PREFIX + cacheKey + ".mp3";
        return r2StorageService.fileExists(r2Key);
    }
    
    /**
     * Supprime un fichier audio du cache
     */
    public boolean deleteFromCache(String cacheKey) {
        String r2Key = TTS_CACHE_PREFIX + cacheKey + ".mp3";
        return r2StorageService.deleteFile(r2Key);
    }
}