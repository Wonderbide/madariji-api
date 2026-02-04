package com.backcover.controller;

import com.backcover.config.LanguageConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des langues supportées
 * dans le système de traduction.
 */
@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private final LanguageConfig languageConfig;

    public LanguageController(LanguageConfig languageConfig) {
        this.languageConfig = languageConfig;
    }

    /**
     * Retourne la liste des langues supportées pour la traduction
     */
    @GetMapping("/supported")
    public ResponseEntity<Map<String, LanguageInfo>> getSupportedLanguages() {
        Map<String, LanguageInfo> supportedLanguages = languageConfig.getSupported()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new LanguageInfo(
                                entry.getValue().getName(),
                                entry.getValue().getDisplayName(),
                                entry.getValue().getLocale()
                        )
                ));

        return ResponseEntity.ok(supportedLanguages);
    }

    /**
     * Retourne la langue par défaut du système
     */
    @GetMapping("/default")
    public ResponseEntity<Map<String, String>> getDefaultLanguage() {
        String defaultLang = languageConfig.getDefaultLanguage();
        LanguageConfig.LanguageInfo defaultInfo = languageConfig.getLanguageInfo(defaultLang);
        
        Map<String, String> response = Map.of(
                "code", defaultLang,
                "name", defaultInfo.getName(),
                "displayName", defaultInfo.getDisplayName(),
                "locale", defaultInfo.getLocale()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * DTO pour les informations de langue exposées via l'API
     */
    public static class LanguageInfo {
        private final String name;
        private final String displayName;
        private final String locale;

        public LanguageInfo(String name, String displayName, String locale) {
            this.name = name;
            this.displayName = displayName;
            this.locale = locale;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getLocale() {
            return locale;
        }
    }
}