// Dans com/backcover/controller/UserController.java
package com.backcover.controller;

import com.backcover.config.LanguageConfig;
import com.backcover.dto.ConsultedWordDetailsDto;
import com.backcover.dto.UserDto;
import com.backcover.dto.UserProfileDto;
import com.backcover.model.User;
import com.backcover.service.UserService;
import com.backcover.service.UserWordListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserWordListService userWordListService;
    private final LanguageConfig languageConfig;

    public UserController(UserService userService, UserWordListService userWordListService, LanguageConfig languageConfig) {
        this.userService = userService;
        this.userWordListService = userWordListService;
        this.languageConfig = languageConfig;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            log.warn("Attempt to get current user without JWT.");
            return ResponseEntity.status(401).build();
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }
        
        log.info("Getting current user profile for email: {}", email);
        
        User user = userService.findOrCreateUserByEmail(email);
        
        UserProfileDto profile = new UserProfileDto();
        profile.setId(user.getId());
        profile.setEmail(user.getEmail());
        profile.setSupabaseUserId(user.getSupabaseUserId());
        profile.setRole(user.getRole());
        profile.setCreatedAt(user.getCreatedAt());
        profile.setUpdatedAt(user.getUpdatedAt());
        
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/sync")
    public ResponseEntity<UserDto> syncUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            // Ne devrait pas arriver si l'endpoint est correctement sécurisé
            log.warn("Attempt to sync user without JWT.");
            return ResponseEntity.status(401).build();
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        log.info("Syncing user for email: {}", email);
        User syncedUser = userService.findOrCreateUserByEmail(email);

        // Mapper l'entité User vers un UserDto simple pour la réponse
        UserDto userDto = new UserDto(
                syncedUser.getId(),
                syncedUser.getEmail(),
                syncedUser.getSupabaseUserId()
                // Ajoutez d'autres champs si nécessaire pour le frontend après la synchro
        );

        log.info("User synced successfully. Local ID: {}, Email: {}", syncedUser.getId(), email);
        return ResponseEntity.ok(userDto);
    }

    /**
     * @deprecated Utiliser GET /api/user/wordlist/book/{bookId}/items/details à la place.
     * Cet endpoint utilise un DTO incompatible avec le frontend et dépend d'entités obsolètes.
     */
    @Deprecated
    @GetMapping("/me/books/{bookId}/consulted-words-details")
    public ResponseEntity<List<ConsultedWordDetailsDto>> getConsultedWordsWithDetails(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @PathVariable UUID bookId,
            @RequestParam(value = "targetLanguageCode", defaultValue = "fr") String targetLanguageCode) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }
        
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        log.info("CONTROLLER: Getting consulted words with details - bookId: {}, email: {}, targetLanguageCode: {} (normalized: {})", 
                bookId, email, targetLanguageCode, normalizedLanguage);

        try {
            List<ConsultedWordDetailsDto> consultedWords = userWordListService
                    .getConsultedWordsWithDetails(email, bookId, normalizedLanguage);
            
            log.info("CONTROLLER: Successfully retrieved {} consulted words with details for book {} and user {}", 
                    consultedWords.size(), bookId, email);
            return ResponseEntity.ok(consultedWords);
            
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException as-is (with proper HTTP status)
            throw e;
        } catch (Exception e) {
            log.error("CONTROLLER: Unexpected error while retrieving consulted words for book {} and user {}: {}", 
                    bookId, email, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to retrieve consulted words due to a server error.");
        }
    }

}