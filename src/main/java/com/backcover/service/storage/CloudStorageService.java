package com.backcover.service.storage;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface pour les services de stockage cloud
 * (Copie indépendante de l'interface du batch processor)
 */
public interface CloudStorageService {
    
    /**
     * Upload un fichier vers le cloud storage
     */
    void uploadFile(String key, byte[] content, Map<String, String> metadata);
    
    /**
     * Télécharge un fichier depuis le cloud storage
     */
    Optional<byte[]> downloadFile(String key);
    
    /**
     * Vérifie si un fichier existe
     */
    boolean fileExists(String key);
    
    /**
     * Supprime un fichier
     */
    boolean deleteFile(String key);
    
    /**
     * Liste les fichiers avec un préfixe donné
     */
    List<String> listFiles(String prefix, int maxResults);
    
    /**
     * Copie un fichier vers une nouvelle clé
     */
    boolean copyFile(String sourceKey, String destKey);
    
    /**
     * Génère une URL présignée pour téléchargement
     */
    String generatePresignedUrl(String key, Duration duration);
}