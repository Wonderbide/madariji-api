// src/main/java/com/backcover/util/security/AuthenticationHelper.java
package com.backcover.util.security;

import com.backcover.auth.port.UserAuthenticationPort;
import com.backcover.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authentication helper that follows hexagonal architecture.
 * Uses UserAuthenticationPort to abstract authentication logic.
 */
@Component
public class AuthenticationHelper {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHelper.class);
    private final UserAuthenticationPort authenticationPort;

    public AuthenticationHelper(UserAuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    /**
     * Get email from JWT - provider agnostic.
     * @deprecated Use getEmail(Jwt) instead
     */
    @Deprecated
    public String getSupabaseUserId(Jwt jwtPrincipal) {
        return getEmail(jwtPrincipal);
    }
    
    /**
     * @deprecated Use getEmail(Jwt) instead
     */
    @Deprecated
    public String getAuth0UserId(Jwt jwtPrincipal) {
        return getEmail(jwtPrincipal);
    }

    /**
     * Extract email from JWT - this is now the primary identifier.
     * @param jwtPrincipal The JWT injected via @AuthenticationPrincipal.
     * @return The user's email.
     * @throws ResponseStatusException if JWT is null or email is missing.
     */
    public String getEmail(Jwt jwtPrincipal) {
        if (jwtPrincipal == null) {
            log.warn("Attempted to get email from null JWT. Authentication is probably missing.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required (JWT token missing).");
        }
        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            log.warn("JWT does not contain email claim");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: email claim missing.");
        }
        return email;
    }

    /**
     * Get the authenticated user entity using email as the primary identifier.
     * Creates the user if they don't exist (auto-sync).
     * @param jwtPrincipal The JWT injected via @AuthenticationPrincipal.
     * @return The local User entity.
     */
    public User getRequiredAuthenticatedUser(Jwt jwtPrincipal) {
        if (jwtPrincipal == null) {
            log.warn("No JWT provided. Authentication required.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required (JWT token missing).");
        }
        
        try {
            // Delegate to the authentication port
            return authenticationPort.authenticate(jwtPrincipal.getTokenValue());
        } catch (IllegalArgumentException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Get email from Authentication object.
     * @deprecated Use getEmail(Authentication) instead
     */
    @Deprecated
    public String getSupabaseUserId(Authentication authentication) {
        return getEmail(authentication);
    }

    /**
     * Check if the authenticated user has admin role.
     * @param jwtPrincipal The JWT injected via @AuthenticationPrincipal.
     * @return true if the user has ROLE_ADMIN, false otherwise.
     */
    public boolean isAdmin(Jwt jwtPrincipal) {
        try {
            User user = getRequiredAuthenticatedUser(jwtPrincipal);
            return "ROLE_ADMIN".equals(user.getRole());
        } catch (Exception e) {
            log.debug("Failed to check admin role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Require the authenticated user to have admin role.
     * @param jwtPrincipal The JWT injected via @AuthenticationPrincipal.
     * @throws ResponseStatusException if user is not admin.
     */
    public void requireAdmin(Jwt jwtPrincipal) {
        if (!isAdmin(jwtPrincipal)) {
            log.warn("Access denied: User does not have admin role");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    /**
     * Extract email from Authentication object.
     * @param authentication The Authentication object provided by Spring Security.
     * @return The user's email.
     * @throws ResponseStatusException if authentication is invalid.
     */
    public String getEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return getEmail(jwt);
        }
        log.warn("The authenticated principal is not a JWT instance or Authentication object is null.");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication object.");
    }
    
    /**
     * Get the authenticated user from Authentication object.
     * @param authentication The Authentication object.
     * @return The authenticated User.
     */
    public User getRequiredAuthenticatedUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return getRequiredAuthenticatedUser(jwt);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication object.");
    }
}