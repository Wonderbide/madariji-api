// Dans com/backcover/repository/UserBookProgressRepository.java
package com.backcover.repository;

import com.backcover.model.UserBookProgress;
import com.backcover.model.UserBookProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBookProgressRepository extends JpaRepository<UserBookProgress, UserBookProgressId> {

    // Spring Data JPA va automatiquement générer l'implémentation pour :
    // - save(UserBookProgress entity) -> pour créer ou mettre à jour une progression
    // - findById(UserBookProgressId id) -> pour récupérer une progression par son ID composite (userId, bookId)
    // - delete(UserBookProgress entity) -> pour supprimer une progression
    // - findAll(), etc.

    // Méthode personnalisée potentiellement utile pour trouver la progression par ID utilisateur et ID de livre directement
    // Bien que findById(new UserBookProgressId(userId, bookId)) fonctionne déjà.
    // Cette méthode est juste un alias plus explicite si vous le souhaitez.
    Optional<UserBookProgress> findByUserIdAndBookId(UUID user_id, UUID book_id);
    
    /**
     * Delete all progress by user ID (for test cleanup)
     */
    void deleteByUserId(UUID userId);

}