// Dans com/backcover/controller/ReadingActivityController.java
package com.backcover.controller;

// Import de votre DTO (adaptez le chemin si nécessaire)
import com.backcover.dto.UpdateProgressRequest; // Supposant que vous l'avez mis dans dto/readingactivity

// Imports pour les services
import com.backcover.service.ReadingActivityService;
import com.backcover.service.UserService;

// Import de VOTRE entité User
import com.backcover.model.User; // <<< IMPORT AJOUTÉ ICI

// Imports pour la validation
import jakarta.validation.Valid;
// jakarta.validation.constraints.Min et NotNull sont utilisés dans le DTO, donc pas besoin ici si le DTO est bien annoté.

// Imports Spring Framework
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*; // Pour @PutMapping, @RequestBody, etc.
import org.springframework.web.server.ResponseStatusException;

// Import pour UUID
import java.util.UUID;

@RestController
@RequestMapping("/api/reading-activity")
public class ReadingActivityController {

    private final ReadingActivityService readingActivityService;
    private final UserService userService;

    public ReadingActivityController(ReadingActivityService readingActivityService, UserService userService) {
        this.readingActivityService = readingActivityService;
        this.userService = userService;
    }

    @PutMapping("/books/{bookId}/progress")
    public ResponseEntity<Void> saveBookProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId,
            @Valid @RequestBody UpdateProgressRequest request) { // UpdateProgressRequest est maintenant importé

        // Get email from JWT as primary identifier
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        User currentUser = userService.findOrCreateUserByEmail(email);
        UUID userId = currentUser.getId();

        readingActivityService.saveOrUpdateProgress(userId, bookId, request.getPageNumber());
        return ResponseEntity.ok().build();
    }
}