package com.backcover.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * Configuration centralisée pour la gestion des langues supportées
 * dans le pipeline de traduction de mots.
 */
@Configuration
@ConfigurationProperties(prefix = "translation.languages")
public class LanguageConfig {

    /**
     * Mapping des codes langue ISO vers les noms des langues
     * Utilisé pour construire les prompts d'analyse
     */
    private Map<String, LanguageInfo> supported = Map.of(
            "fr", new LanguageInfo("FRANÇAISE", "français", "fr-FR"),
            "en", new LanguageInfo("ANGLAISE", "anglais", "en-US"),
            "ar", new LanguageInfo("ARABE", "arabe", "ar-SA"),
            "es", new LanguageInfo("ESPAGNOLE", "espagnol", "es-ES"),
            "de", new LanguageInfo("ALLEMANDE", "allemand", "de-DE"),
            "it", new LanguageInfo("ITALIENNE", "italien", "it-IT"),
            "pt", new LanguageInfo("PORTUGAISE", "portugais", "pt-BR"),
            "ru", new LanguageInfo("RUSSE", "russe", "ru-RU"),
            "zh", new LanguageInfo("CHINOISE", "chinois", "zh-CN"),
            "ja", new LanguageInfo("JAPONAISE", "japonais", "ja-JP")
    );

    /**
     * Langue par défaut si aucune n'est spécifiée ou si la langue demandée n'est pas supportée
     */
    private String defaultLanguage = "fr";

    /**
     * Indique si on doit lever une exception pour les langues non supportées
     * ou utiliser la langue par défaut
     */
    private boolean strictValidation = false;

    // Getters et Setters
    public Map<String, LanguageInfo> getSupported() {
        return supported;
    }

    public void setSupported(Map<String, LanguageInfo> supported) {
        this.supported = supported;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public boolean isStrictValidation() {
        return strictValidation;
    }

    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }

    // Méthodes utilitaires
    public boolean isLanguageSupported(String languageCode) {
        return languageCode != null && supported.containsKey(languageCode.toLowerCase());
    }

    public LanguageInfo getLanguageInfo(String languageCode) {
        return supported.get(languageCode.toLowerCase());
    }

    public String getLanguageName(String languageCode) {
        LanguageInfo info = getLanguageInfo(languageCode);
        return info != null ? info.getName() : supported.get(defaultLanguage).getName();
    }

    public Set<String> getSupportedLanguageCodes() {
        return supported.keySet();
    }

    /**
     * Valide et normalise un code langue
     * @param languageCode Le code langue à valider
     * @return Le code langue normalisé ou la langue par défaut
     * @throws IllegalArgumentException Si la validation stricte est activée et la langue non supportée
     */
    public String validateAndNormalize(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return defaultLanguage;
        }

        String normalized = languageCode.toLowerCase().trim();
        
        if (isLanguageSupported(normalized)) {
            return normalized;
        }

        if (strictValidation) {
            throw new IllegalArgumentException(
                String.format("Langue '%s' non supportée. Langues disponibles: %s", 
                    languageCode, getSupportedLanguageCodes())
            );
        }

        return defaultLanguage;
    }

    /**
     * Classe interne pour stocker les informations d'une langue
     */
    public static class LanguageInfo {
        private final String name;         // Nom en majuscules pour les prompts (ex: "FRANÇAISE")
        private final String displayName;  // Nom d'affichage (ex: "français")
        private final String locale;       // Locale complet (ex: "fr-FR")

        public LanguageInfo(String name, String displayName, String locale) {
            this.name = name;
            this.displayName = displayName;
            this.locale = locale;
        }

        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getLocale() { return locale; }
    }
}