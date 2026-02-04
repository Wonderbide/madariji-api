// com.backcover.model.User.java
package com.backcover.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID; // <<< Importer UUID

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Fait générer l'UUID par Hibernate (côté application)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // <<< Changer le type de Long à UUID

    @Column(nullable = false, unique = true)
    private String email; // Primary identifier for authentication

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(nullable = false)
    private String role = "ROLE_FREE";

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "subscription_status")
    private String subscriptionStatus = "none";

    public User() {
        // Si la base de données génère l'UUID (ce qui est recommandé),
        // l'ID sera null ici et défini lors de la persistance.
        // Si vous voulez générer l'UUID côté application :
        // this.id = UUID.randomUUID(); // Mais il est préférable que la DB s'en charge
    }

    public User(String email) {
        this.email = email;
    }

    // --- Getters et Setters ---
    public UUID getId() { return id; } // <<< Type de retour changé en UUID
    public void setId(UUID id) { this.id = id; } // <<< Type de paramètre changé en UUID

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // Deprecated compatibility method - returns email as the identifier
    @Deprecated
    public String getSupabaseUserId() { 
        return email; // Using email as the primary identifier now
    }
    @Deprecated
    public void setSupabaseUserId(String supabaseUserId) { 
        // Ignored - we no longer store external IDs
    }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }

    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) { this.stripeSubscriptionId = stripeSubscriptionId; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    // --- Callbacks JPA ---
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        // Si l'ID n'est pas encore défini et que vous ne comptez PAS sur la DB pour le générer,
        // vous pourriez l'initialiser ici :
        // if (this.id == null) {
        //     this.id = UUID.randomUUID();
        // }
        // Mais, comme pour l'entité Book, il est préférable que la base de données
        // s'occupe de générer l'UUID (via une valeur par défaut comme gen_random_uuid()).
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // --- equals() et hashCode() ---
    // Baser equals/hashCode sur l'ID est une pratique courante APRES la persistance.
    // Votre méthode actuelle basée sur supabaseUserId est aussi valide et unique.
    // Si vous changez pour l'ID UUID, cela ressemblerait à ceci :
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Email est maintenant notre identifiant métier unique
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", subscriptionStatus='" + subscriptionStatus + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}