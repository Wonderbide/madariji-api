// Dans com/backcover/service/BookProcessingErrorHelper.java
package com.backcover.service;

import com.backcover.model.Book;
import com.backcover.model.BookStatus;
import com.backcover.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component; // Ou @Service
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component // Ou @Service
public class BookProcessingErrorHelper {
    private static final Logger log = LoggerFactory.getLogger(BookProcessingErrorHelper.class);
    private final BookRepository bookRepository;

    public BookProcessingErrorHelper(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBookStatusToFailed(UUID bookId, String details) {
        log.warn("[BookProcessingErrorHelper] Marquage du livre {} en FAILED. Détails: {}", bookId, details);
        try {
            Book bookToUpdate = bookRepository.findById(bookId)
                    .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé pour mise à jour statut FAILED: " + bookId));
            bookToUpdate.setStatus(BookStatus.FAILED);
            bookToUpdate.setProcessingDetails(details);
            bookRepository.save(bookToUpdate);
            log.info("[BookProcessingErrorHelper] Livre {} marqué comme FAILED.", bookId);
        } catch (Exception ex) {
            log.error("[BookProcessingErrorHelper] CRITIQUE: Échec de la mise à jour du statut FAILED pour le livre ID {}: {}", bookId, ex.getMessage(), ex);
        }
    }
}