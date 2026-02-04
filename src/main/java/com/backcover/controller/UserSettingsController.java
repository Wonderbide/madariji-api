package com.backcover.controller;

import com.backcover.dto.user.UserSettingsDto;
import com.backcover.service.UserSettingsService;
import com.backcover.util.security.AuthenticationHelper; // En supposant que vous avez cet utilitaire
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/settings") // Endpoint de base pour les paramètres utilisateur
public class UserSettingsController {

    private final UserSettingsService userSettingsService;
    private final AuthenticationHelper authenticationHelper; // Pour obtenir l'auth0Id

    @Autowired
    public UserSettingsController(UserSettingsService userSettingsService, AuthenticationHelper authenticationHelper) {
        this.userSettingsService = userSettingsService;
        this.authenticationHelper = authenticationHelper;
    }

    /**
     * GET /api/user/settings
     * Récupère les paramètres de l'utilisateur actuellement authentifié.
     * Crée des paramètres par défaut s'ils n'existent pas.
     */
    @GetMapping
    public ResponseEntity<UserSettingsDto> getUserSettings(@AuthenticationPrincipal Jwt jwtPrincipal) {
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        UserSettingsDto settingsDto = userSettingsService.getUserSettings(supabaseUserId);
        return ResponseEntity.ok(settingsDto);
    }

    /**
     * PUT /api/user/settings
     * Met à jour les paramètres de l'utilisateur actuellement authentifié.
     */
    @PutMapping
    public ResponseEntity<UserSettingsDto> updateUserSettings(@AuthenticationPrincipal Jwt jwtPrincipal, @RequestBody UserSettingsDto settingsDto) {
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        UserSettingsDto updatedSettingsDto = userSettingsService.updateUserSettings(supabaseUserId, settingsDto);
        return ResponseEntity.ok(updatedSettingsDto);
    }
}