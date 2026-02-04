// com.backcover.service.UserService.java - Gestion de la Race Condition findOrCreateUser

package com.backcover.service;

import com.backcover.model.User;
import com.backcover.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // <<< Importer l'exception
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Find user by email (primary identifier)
    public Optional<User> findUserByEmail(String email) {
        log.info("Attempting to find user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    // Deprecated method for backward compatibility
    @Deprecated
    public Optional<User> findUserBySupabaseId(String supabaseUserId) {
        log.warn("findUserBySupabaseId is deprecated. Use findUserByEmail instead.");
        // Return empty since we no longer have this column
        return Optional.empty();
    }

    // Public method to find or create user by email with race condition handling
    public User findOrCreateUserByEmail(String email) {
        log.info("Attempting to find or create user for email: {}", email);
        
        // First try to get existing user
        Optional<User> existingUser = findUserByEmail(email);
        if (existingUser.isPresent()) {
            log.info("Found existing user with ID {} for email {}", existingUser.get().getId(), email);
            return existingUser.get();
        }
        
        // If user doesn't exist, try to create
        try {
            return createUser(email);
        } catch (DataIntegrityViolationException e) {
            // In case of race condition, retry fetching user
            log.warn("DataIntegrityViolationException during user creation for email {}, likely race condition. Re-fetching user.", email);
            
            // New transaction for search
            Optional<User> userCreatedByOtherThread = findUserByEmail(email);
            if (userCreatedByOtherThread.isPresent()) {
                log.info("Found user created by concurrent thread for email {}.", email);
                return userCreatedByOtherThread.get();
            } else {
                log.error("CRITICAL: User for email {} not found even after DataIntegrityViolationException during creation!", email);
                throw new RuntimeException("Failed to find or create user after potential race condition for email: " + email, e);
            }
        }
    }
    
    // Deprecated method for backward compatibility
    @Deprecated
    public User findOrCreateUser(String supabaseUserId, String emailFromToken) {
        log.warn("findOrCreateUser with supabaseUserId is deprecated. Use findOrCreateUserByEmail instead.");
        return findOrCreateUserByEmail(emailFromToken);
    }
    
    // Transactional method to create user by email
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User createUser(String email) {
        log.info("Creating new user for email: {}", email);
        
        User newUser = new User(email);
        
        User savedUser = userRepository.saveAndFlush(newUser);
        log.info("Successfully created new user with ID {} for email: {}", savedUser.getId(), email);
        
        return savedUser;
    }
    
    // Deprecated method for backward compatibility
    @Deprecated
    public User createUser(String supabaseUserId, String emailFromToken) {
        log.warn("createUser with supabaseUserId is deprecated. Use createUser(email) instead.");
        return createUser(emailFromToken);
    }
    
    // Find user by Supabase ID - for backward compatibility, search by email
    public Optional<User> findBySupabaseUserId(String supabaseUserId) {
        // Since we removed the supabase_user_id column, this will always return empty
        // Controllers should migrate to use findUserByEmail instead
        return Optional.empty();
    }
}