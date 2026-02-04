package com.backcover.controller;

import com.backcover.model.Book;
import com.backcover.model.BookStatus;
import com.backcover.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller pour recevoir les webhooks depuis les workers externes
 * Ces endpoints sont publics (configurés dans SecurityConfig)
 */
@RestController
@RequestMapping("/batch/webhooks")
public class BatchWebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(BatchWebhookController.class);
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BatchWebhookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * Webhook appelé quand un nouveau PDF est uploadé
     */
    @PostMapping("/new-book")
    public ResponseEntity<Map<String, Object>> handleNewBook(@RequestBody Map<String, Object> payload) {
        String bookId = (String) payload.get("bookId");
        String r2Key = (String) payload.get("r2Key");
        
        log.info("[WEBHOOK] Nouveau livre reçu: bookId={}, r2Key={}", bookId, r2Key);
        
        // Webhook de notification seulement - enrichissement géré par AsyncEnrichmentService
        return ResponseEntity.ok(Map.of(
            "status", "acknowledged",
            "bookId", bookId,
            "message", "New book notification received"
        ));
    }
    
    /**
     * Webhook appelé quand les résultats d'enrichissement sont prêts
     */
    @PostMapping("/result-ready")
    public ResponseEntity<Map<String, Object>> handleResultReady(@RequestBody Map<String, Object> payload) {
        String bookId = (String) payload.get("bookId");
        String r2Key = (String) payload.get("r2Key");

        log.info("[WEBHOOK] Résultats prêts: bookId={}, r2Key={}", bookId, r2Key);

        // Webhook de notification seulement - enrichissement géré par AsyncEnrichmentService
        return ResponseEntity.ok(Map.of(
            "status", "acknowledged",
            "bookId", bookId,
            "message", "Results notification received"
        ));
    }
    
    /**
     * Webhook appelé quand le fichier structured est créé (enrichissement terminé)
     */
    @PostMapping("/structured-ready")
    @Transactional
    public ResponseEntity<Map<String, Object>> handleStructuredReady(@RequestBody Map<String, Object> payload) {
        String bookIdStr = (String) payload.get("bookId");
        String r2Key = (String) payload.get("r2Key");
        String uploadedAt = (String) payload.get("uploadedAt");
        
        log.info("[WEBHOOK] ✅ Fichier structured prêt: bookId={}, r2Key={}", bookIdStr, r2Key);
        
        try {
            UUID bookId = UUID.fromString(bookIdStr);
            
            // Chercher le livre dans la base de données
            Book book = bookRepository.findById(bookId).orElse(null);
            
            if (book == null) {
                log.warn("[WEBHOOK] Livre non trouvé dans backcover: {}", bookId);
                return ResponseEntity.ok(Map.of(
                    "status", "not_found",
                    "bookId", bookIdStr,
                    "message", "Book not found in backcover database"
                ));
            }
            
            // Vérifier le statut actuel
            BookStatus currentStatus = book.getStatus();
            log.info("[WEBHOOK] Statut actuel du livre {}: {}", bookId, currentStatus);
            
            // Mettre à jour le statut seulement si le livre n'est pas déjà complété
            if (currentStatus != BookStatus.COMPLETED && 
                currentStatus != BookStatus.COMPLETED_WITH_ERRORS) {
                
                book.setStatus(BookStatus.COMPLETED);
                book.setProcessingDetails("Enrichissement terminé via Gemini batch processing");
                bookRepository.save(book);
                
                log.info("[WEBHOOK] ✅ Livre {} mis à jour: {} -> COMPLETED", bookId, currentStatus);
                
                return ResponseEntity.ok(Map.of(
                    "status", "updated",
                    "bookId", bookIdStr,
                    "previousStatus", currentStatus.toString(),
                    "newStatus", "COMPLETED",
                    "message", "Book status updated to COMPLETED"
                ));
            } else {
                log.info("[WEBHOOK] Livre {} déjà complété, pas de mise à jour nécessaire", bookId);
                
                return ResponseEntity.ok(Map.of(
                    "status", "already_completed",
                    "bookId", bookIdStr,
                    "currentStatus", currentStatus.toString(),
                    "message", "Book already completed, no update needed"
                ));
            }
            
        } catch (IllegalArgumentException e) {
            log.error("[WEBHOOK] ID de livre invalide: {}", bookIdStr, e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "bookId", bookIdStr,
                "message", "Invalid book ID format"
            ));
        } catch (Exception e) {
            log.error("[WEBHOOK] Erreur lors du traitement: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "bookId", bookIdStr,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }
}