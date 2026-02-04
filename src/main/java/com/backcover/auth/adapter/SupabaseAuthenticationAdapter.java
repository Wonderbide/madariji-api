package com.backcover.auth.adapter;

import com.backcover.auth.port.UserAuthenticationPort;
import com.backcover.model.User;
import com.backcover.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Supabase authentication adapter implementing the hexagonal architecture pattern.
 * This adapter handles JWT validation and user management for Supabase authentication.
 */
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = "supabase", matchIfMissing = true)
public class SupabaseAuthenticationAdapter implements UserAuthenticationPort {
    
    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthenticationAdapter.class);
    
    private final UserRepository userRepository;
    private final JwtDecoder jwtDecoder;
    
    public SupabaseAuthenticationAdapter(
            UserRepository userRepository,
            JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.jwtDecoder = jwtDecoder;
    }
    
    @Override
    @Transactional
    public User authenticate(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String email = extractEmail(jwt);
            String name = extractName(jwt);
            
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email not found in token");
            }
            
            return findOrCreateUser(email, name);
            
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new IllegalArgumentException("Authentication failed", e);
        }
    }
    
    @Override
    @Transactional
    public User findOrCreateUser(String email, String name) {
        // First attempt: try to find existing user
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Second attempt: try to create new user
        try {
            log.info("Creating new user with email: {}", email);
            User newUser = new User(email);
            // Set name if available
            if (name != null && !name.isBlank()) {
                // Add name field to User model if needed
            }
            return userRepository.save(newUser);
        } catch (Exception e) {
            // Handle race condition: another thread may have created the user
            log.debug("User creation failed (likely already exists), attempting to find again: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException(
                            "Failed to create user and user not found: " + email, e));
        }
    }
    
    @Override
    public String extractEmail(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return extractEmail(jwt);
        } catch (Exception e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String extractEmail(Jwt jwt) {
        // Supabase stores email in the "email" claim
        return jwt.getClaimAsString("email");
    }
    
    private String extractName(Jwt jwt) {
        // Try different possible name claims
        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("full_name");
        }
        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("user_name");
        }
        return name;
    }
}