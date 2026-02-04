// Dans com/backcover/service/ReadingActivityService.java
package com.backcover.service;

import com.backcover.model.Book;
import com.backcover.model.User;
import com.backcover.model.UserBookProgress;
import com.backcover.model.UserBookProgressId;
import com.backcover.repository.BookRepository;
import com.backcover.repository.UserBookProgressRepository;
import com.backcover.repository.UserRepository; // Supposant que vous voudrez peut-être valider l'utilisateur
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReadingActivityService {

    private final UserBookProgressRepository userBookProgressRepository;
    private final UserRepository userRepository; // Optionnel, pour valider que l'user existe
    private final BookRepository bookRepository; // Optionnel, pour valider que le livre existe

    // Constructeur pour l'injection de dépendances
    public ReadingActivityService(UserBookProgressRepository userBookProgressRepository,
                                  UserRepository userRepository,
                                  BookRepository bookRepository) {
        this.userBookProgressRepository = userBookProgressRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Sauvegarde ou met à jour la progression de lecture d'un utilisateur pour un livre donné.
     *
     * @param userId L'ID de l'utilisateur.
     * @param bookId L'ID du livre.
     * @param pageNumber Le numéro de la dernière page lue.
     * @throws EntityNotFoundException si l'utilisateur ou le livre n'est pas trouvé (si la validation est active).
     */
    @Transactional
    public void saveOrUpdateProgress(UUID userId, UUID bookId, int pageNumber) {
        // Optionnel : Valider que l'utilisateur et le livre existent
        // Cela dépend si vous voulez que cette validation soit ici ou plus haut (Controller/Security)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        UserBookProgressId progressId = new UserBookProgressId(userId, bookId);
        Optional<UserBookProgress> existingProgressOpt = userBookProgressRepository.findById(progressId);

        UserBookProgress progress;
        if (existingProgressOpt.isPresent()) {
            progress = existingProgressOpt.get();
            progress.setLastReadPageNumber(pageNumber);
            // L'annotation @PreUpdate dans UserBookProgress s'occupera de updatedAt
        } else {
            // Utiliser les entités User et Book récupérées pour la nouvelle instance
            progress = new UserBookProgress(user, book, pageNumber);
            // L'annotation @PrePersist dans UserBookProgress s'occupera de updatedAt
        }
        userBookProgressRepository.save(progress);
    }

    /**
     * Récupère le numéro de la dernière page lue par un utilisateur pour un livre donné.
     *
     * @param userId L'ID de l'utilisateur.
     * @param bookId L'ID du livre.
     * @return Un Optional contenant le numéro de la page si une progression existe, sinon Optional.empty().
     */
    @Transactional(readOnly = true) // Indique que cette méthode ne modifie pas de données
    public Optional<Integer> getLastReadPage(UUID userId, UUID bookId) {
        UserBookProgressId progressId = new UserBookProgressId(userId, bookId);
        return userBookProgressRepository.findById(progressId)
                .map(UserBookProgress::getLastReadPageNumber);
    }
}