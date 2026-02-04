package com.backcover.service;

import com.backcover.service.storage.R2StorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les couvertures de livres dans R2
 * Extrait la première page du PDF et la stocke comme image de couverture
 */
@Service
public class BookCoverService {
    
    private static final Logger log = LoggerFactory.getLogger(BookCoverService.class);
    private static final String COVERS_PREFIX = "books/covers/";
    private static final int DEFAULT_DPI = 150;
    
    private final R2StorageService r2StorageService;
    
    public BookCoverService(@Qualifier("mainR2Storage") R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
    }
    
    /**
     * Génère et sauvegarde la couverture d'un livre dans R2
     * @param pdfContent Le contenu du PDF en bytes
     * @param bookId L'ID du livre
     * @return Le chemin R2 de la couverture sauvegardée, ou null si échec
     */
    public String generateAndSaveCover(byte[] pdfContent, UUID bookId) {
        try {
            log.info("[COVER-SERVICE] Génération de la couverture pour le livre {}", bookId);
            
            // Extraire la première page comme image
            byte[] coverImage = extractFirstPageAsImage(pdfContent);
            if (coverImage == null || coverImage.length == 0) {
                log.warn("[COVER-SERVICE] Impossible d'extraire la couverture du PDF pour le livre {}", bookId);
                return null;
            }
            
            // Construire le chemin R2
            String coverKey = COVERS_PREFIX + bookId + ".png";
            
            // Préparer les métadonnées
            Map<String, String> metadata = new HashMap<>();
            metadata.put("content-type", "image/png");
            metadata.put("book-id", bookId.toString());
            metadata.put("type", "book-cover");
            
            // Sauvegarder dans R2
            r2StorageService.uploadFile(coverKey, coverImage, metadata);
            
            log.info("[COVER-SERVICE] ✅ Couverture sauvegardée dans R2: {} ({} KB)", 
                    coverKey, coverImage.length / 1024);
            
            return coverKey;
            
        } catch (Exception e) {
            log.error("[COVER-SERVICE] ❌ Erreur lors de la génération de la couverture pour le livre {}: {}", 
                    bookId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extrait la première page du PDF comme image PNG
     */
    private byte[] extractFirstPageAsImage(byte[] pdfContent) {
        try (PDDocument document = PDDocument.load(pdfContent)) {
            if (document.getNumberOfPages() == 0) {
                log.warn("[COVER-SERVICE] Le PDF n'a aucune page");
                return null;
            }
            
            // Rendre la première page
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, DEFAULT_DPI, ImageType.RGB);
            
            // Convertir en bytes PNG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean written = ImageIO.write(image, "png", baos);
            
            if (!written) {
                log.error("[COVER-SERVICE] Impossible d'écrire l'image PNG");
                return null;
            }
            
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("[COVER-SERVICE] Erreur lors de l'extraction de la première page: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Récupère la couverture d'un livre depuis R2
     */
    public Optional<byte[]> getCover(UUID bookId) {
        String coverKey = COVERS_PREFIX + bookId + ".png";
        return r2StorageService.downloadFile(coverKey);
    }
    
    /**
     * Génère une URL présignée pour accéder directement à la couverture
     */
    public String generateCoverUrl(UUID bookId, Duration duration) {
        String coverKey = COVERS_PREFIX + bookId + ".png";
        return r2StorageService.generatePresignedUrl(coverKey, duration);
    }
    
    /**
     * Vérifie si une couverture existe pour un livre
     */
    public boolean coverExists(UUID bookId) {
        String coverKey = COVERS_PREFIX + bookId + ".png";
        return r2StorageService.fileExists(coverKey);
    }
    
    /**
     * Supprime la couverture d'un livre
     */
    public boolean deleteCover(UUID bookId) {
        String coverKey = COVERS_PREFIX + bookId + ".png";
        return r2StorageService.deleteFile(coverKey);
    }
}