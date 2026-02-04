package com.backcover.repository;

import com.backcover.model.User; // Assurez-vous que l'import pointe vers votre package model/entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Primary method to find user by email - the main identifier
    Optional<User> findByEmail(String email);

    // Find user by Stripe customer ID for webhook processing
    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    // Deprecated - kept for backward compatibility during migration
    // Will be removed after migration is complete
    @Deprecated
    default Optional<User> findBySupabaseUserId(String supabaseUserId) {
        // Return empty since we no longer have this column
        return Optional.empty();
    }
    
    // Méthode pour les tests : trouver les utilisateurs de test
    List<User> findByEmailContaining(String emailPattern);

    // Les méthodes CRUD standard (save, findById, etc.) sont héritées de JpaRepository
}