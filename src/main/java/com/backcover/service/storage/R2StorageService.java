package com.backcover.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de stockage CloudFlare R2 pour l'application principale
 * Utilise l'API S3-compatible de R2
 * (Copie indépendante du service R2 du batch processor)
 */
@Service("mainR2Storage")
public class R2StorageService implements CloudStorageService {
    
    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);
    
    @Value("${R2_ENDPOINT}")
    private String endpoint;
    
    @Value("${R2_ACCESS_KEY_ID}")
    private String accessKeyId;
    
    @Value("${R2_SECRET_ACCESS_KEY}")
    private String secretAccessKey;
    
    @Value("${R2_BUCKET_NAME}")
    private String bucketName;
    
    @Value("${R2_REGION:us-east-1}")
    private String region;
    
    private S3Client s3Client;
    private S3Presigner s3Presigner;
    
    @PostConstruct
    public void init() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .chunkedEncodingEnabled(false) // Important pour R2
                    .build())
                .build();
                
            this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
                
            log.info("[R2-STORAGE] ✅ Service R2 initialisé - Bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur initialisation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize R2 storage", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        if (s3Client != null) {
            s3Client.close();
            log.info("[R2-STORAGE] Service R2 fermé");
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
    
    /**
     * Upload un fichier vers R2
     */
    public void uploadFile(String key, byte[] content, Map<String, String> metadata) {
        try {
            // Log détaillé pour debug
            log.debug("[R2-STORAGE] Tentative upload: key='{}', size={} bytes", key, content.length);
            if (metadata != null && !metadata.isEmpty()) {
                log.debug("[R2-STORAGE] Metadata: {}", metadata);
                // Vérifier les caractères spéciaux dans les metadata
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().matches("^[\\x00-\\x7F]*$")) {
                        log.warn("[R2-STORAGE] ⚠️ Metadata '{}' contient des caractères non-ASCII: '{}'", 
                                entry.getKey(), entry.getValue());
                    }
                }
            }
            
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .metadata(metadata != null ? metadata : new HashMap<>());
                
            if (metadata != null && metadata.containsKey("content-type")) {
                requestBuilder.contentType(metadata.get("content-type"));
            }
            
            PutObjectRequest request = requestBuilder.build();
            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(content));
            
            log.debug("[R2-STORAGE] ✅ Upload réussi: {} (ETag: {})", key, response.eTag());
        } catch (S3Exception e) {
            // Log plus détaillé pour les erreurs S3
            log.error("[R2-STORAGE] ❌ Erreur S3 upload '{}': Status={}, Code={}, Message={}", 
                    key, e.statusCode(), e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "N/A", 
                    e.getMessage());
            if (metadata != null && !metadata.isEmpty()) {
                log.error("[R2-STORAGE] Metadata au moment de l'erreur: {}", metadata);
            }
            throw new RuntimeException("Failed to upload to R2: " + key + " - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur inattendue upload {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to upload to R2: " + key, e);
        }
    }
    
    /**
     * Télécharge un fichier depuis R2
     */
    public Optional<byte[]> downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            byte[] content = response.readAllBytes();
            response.close();
            
            log.debug("[R2-STORAGE] ✅ Download réussi: {} ({} bytes)", key, content.length);
            return Optional.of(content);
            
        } catch (NoSuchKeyException e) {
            log.warn("[R2-STORAGE] ⚠️ Fichier non trouvé: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur download {}: {}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Vérifie si un fichier existe
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            s3Client.headObject(request);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur vérification existence {}: {}", key, e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime un fichier
     */
    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            s3Client.deleteObject(request);
            log.debug("[R2-STORAGE] ✅ Fichier supprimé: {}", key);
            return true;
            
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur suppression {}: {}", key, e.getMessage());
            return false;
        }
    }
    
    /**
     * Liste les fichiers avec un préfixe donné
     */
    public List<String> listFiles(String prefix, int maxResults) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .maxKeys(maxResults)
                .build();
                
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            
            List<String> keys = response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
                
            log.debug("[R2-STORAGE] ✅ {} fichiers listés avec préfixe: {}", keys.size(), prefix);
            return keys;
            
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur listing avec préfixe {}: {}", prefix, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Copie un fichier vers une nouvelle clé
     */
    public boolean copyFile(String sourceKey, String destKey) {
        try {
            CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destKey)
                .build();
                
            CopyObjectResponse response = s3Client.copyObject(request);
            log.debug("[R2-STORAGE] ✅ Fichier copié: {} -> {}", sourceKey, destKey);
            return true;
            
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur copie {} -> {}: {}", sourceKey, destKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * Génère une URL présignée pour téléchargement
     */
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();
                
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();
            
            log.debug("[R2-STORAGE] ✅ URL présignée générée pour: {} (valide: {})", key, duration);
            return url;
            
        } catch (Exception e) {
            log.error("[R2-STORAGE] ❌ Erreur génération URL présignée pour {}: {}", key, e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}