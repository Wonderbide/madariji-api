package com.backcover.auth.port;

import com.backcover.model.User;
import java.util.Optional;

/**
 * Port interface for user authentication following hexagonal architecture.
 * This interface defines the contract for authentication adapters,
 * keeping the domain layer independent of specific authentication providers.
 */
public interface UserAuthenticationPort {
    
    /**
     * Authenticate a user from a JWT token.
     * 
     * @param token The JWT token to validate
     * @return The authenticated user
     * @throws IllegalArgumentException if the token is invalid
     */
    User authenticate(String token);
    
    /**
     * Find or create a user based on email.
     * 
     * @param email The user's email (primary identifier)
     * @param name The user's display name (optional)
     * @return The existing or newly created user
     */
    User findOrCreateUser(String email, String name);
    
    /**
     * Extract email from JWT token.
     * 
     * @param token The JWT token
     * @return The email claim from the token
     */
    String extractEmail(String token);
    
    /**
     * Validate if the token is valid.
     * 
     * @param token The JWT token
     * @return true if valid, false otherwise
     */
    boolean isTokenValid(String token);
}