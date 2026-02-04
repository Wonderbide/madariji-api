package com.backcover.service;

import com.backcover.config.LanguageConfig;
import com.backcover.model.prompt.PromptTemplate;
import com.backcover.service.prompt.DynamicPromptResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service pour gérer les templates de prompts multi-langues
 * pour l'analyse de mots arabes.
 * 
 * This service now uses DynamicPromptResolver to fetch prompts from the database
 * instead of hardcoded templates.
 * 
 * NO FALLBACK POLICY: If no prompt is found in database, service will fail.
 */
@Service
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);
    
    private final LanguageConfig languageConfig;
    private final DynamicPromptResolver promptResolver;

    public PromptTemplateService(LanguageConfig languageConfig, 
                               DynamicPromptResolver promptResolver) {
        this.languageConfig = languageConfig;
        this.promptResolver = promptResolver;
    }

    /**
     * Génère le prompt d'analyse de mot pour une langue cible donnée
     * NO FALLBACK: Throws exception if prompt not found in database
     */
    public String buildWordAnalysisPrompt(String wordText, String bookTitle, String bookGenre, String bookDescription, String bookAuthor, String paragraphText, String targetLanguageCode) {
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        String targetLanguageName = languageConfig.getLanguageName(normalizedLanguage);

        log.debug("=== TEMPLATE SERVICE DEBUG ===");
        log.debug("Word Text: '{}'", wordText);
        log.debug("Book Title: '{}'", bookTitle);
        log.debug("Book Genre: '{}'", bookGenre);
        log.debug("Book Author: '{}'", bookAuthor);
        log.debug("Book Description Length: {}", (bookDescription != null ? bookDescription.length() : 0));
        log.debug("Paragraph Text Length: {}", (paragraphText != null ? paragraphText.length() : 0));
        log.debug("Target Language Code: '{}'", targetLanguageCode);
        log.debug("Normalized Language: '{}'", normalizedLanguage);
        log.debug("Target Language Name: '{}'", targetLanguageName);

        try {
            // Resolve multilingual prompt template from database (no language parameter)
            PromptTemplate template = promptResolver.resolvePrompt("WORD_ANALYSIS_V1");

            log.debug("Using prompt template: {} v{}", template.getIdentifier(), template.getVersion());

            // Build the prompt with parameters including target language and book metadata
            return promptResolver.buildPrompt(template,
                    targetLanguageName, // %s - Langue dynamique 1 (dans le titre)
                    wordText,           // %s - Mot Cible 1
                    bookTitle,          // %s - Titre du Livre
                    bookGenre,          // %s - Genre du Livre
                    bookAuthor,         // %s - Auteur du Livre
                    bookDescription,    // %s - Description du Livre
                    paragraphText,      // %s - Contexte de Paragraphe
                    targetLanguageName, // %s - Langue de traduction demandée
                    wordText,           // %s - Mot Cible 2 (répétition pour instructions)
                    targetLanguageName, // %s - Langue dynamique 2 (traduction)
                    wordText,           // %s - Mot Cible 3 (pour règle critique)
                    wordText            // %s - Mot Cible 4 (pour exécution)
            );
        } catch (DynamicPromptResolver.PromptNotFoundException e) {
            log.error("CRITICAL: No active prompt template found for WORD_ANALYSIS_V1. NO FALLBACK ALLOWED.", e);
            throw new IllegalStateException("No active prompt template found for word analysis. Database configuration required.");
        }
    }
}