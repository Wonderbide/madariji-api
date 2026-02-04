package com.backcover.service;

import com.backcover.model.ParagraphContext;
import com.backcover.model.WordContext;
import com.backcover.model.WordTranslation;
import com.backcover.repository.ParagraphContextRepository;
import com.backcover.repository.WordContextRepository;
import com.backcover.repository.WordTranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer la nouvelle architecture de traductions contextuelles.
 * Gère la logique de déduplication et de liaison entre traductions, contextes et instances de mots.
 */
@Service
public class TranslationContextService {

    private static final Logger log = LoggerFactory.getLogger(TranslationContextService.class);

    private final WordTranslationRepository wordTranslationRepository;
    private final ParagraphContextRepository paragraphContextRepository;
    private final WordContextRepository wordContextRepository;

    public TranslationContextService(WordTranslationRepository wordTranslationRepository,
                                   ParagraphContextRepository paragraphContextRepository,
                                   WordContextRepository wordContextRepository) {
        this.wordTranslationRepository = wordTranslationRepository;
        this.paragraphContextRepository = paragraphContextRepository;
        this.wordContextRepository = wordContextRepository;
    }

    /**
     * Trouve ou crée une traduction dans le référentiel.
     * Évite la duplication des traductions textuellement identiques.
     */
    @Transactional
    public WordTranslation findOrCreateTranslation(UUID wordAnalysisId, String languageCode, 
                                                  String translationText, Double confidenceScore, String source) {
        log.debug("Finding or creating translation for analysis {} in {}: '{}'", 
                wordAnalysisId, languageCode, translationText);

        // Chercher une traduction existante
        Optional<WordTranslation> existingTranslation = wordTranslationRepository
                .findByWordAnalysisIdAndLanguageCodeAndTranslationText(wordAnalysisId, languageCode, translationText);

        if (existingTranslation.isPresent()) {
            log.debug("Found existing translation with ID: {}", existingTranslation.get().getId());
            return existingTranslation.get();
        }

        // Créer une nouvelle traduction
        WordTranslation newTranslation = new WordTranslation(wordAnalysisId, languageCode, 
                                                            translationText, confidenceScore, source);
        WordTranslation savedTranslation = wordTranslationRepository.save(newTranslation);
        log.info("Created new translation with ID: {} for analysis {}", savedTranslation.getId(), wordAnalysisId);
        
        return savedTranslation;
    }

    /**
     * Trouve ou crée un contexte de paragraphe dans le référentiel.
     * Évite la duplication des textes de paragraphe identiques.
     */
    @Transactional
    public ParagraphContext findOrCreateParagraphContext(String paragraphText) {
        if (paragraphText == null || paragraphText.trim().isEmpty()) {
            paragraphText = ""; // Normaliser les textes vides
        }

        String contextHash = calculateSHA256Hash(paragraphText);
        log.debug("Finding or creating paragraph context with hash: {}", contextHash);

        // Chercher un contexte existant
        Optional<ParagraphContext> existingContext = paragraphContextRepository.findByContextHash(contextHash);

        if (existingContext.isPresent()) {
            log.debug("Found existing paragraph context with hash: {}", contextHash);
            return existingContext.get();
        }

        // Créer un nouveau contexte
        ParagraphContext newContext = new ParagraphContext(contextHash, paragraphText);
        ParagraphContext savedContext = paragraphContextRepository.save(newContext);
        log.debug("Created new paragraph context with hash: {}", contextHash);
        
        return savedContext;
    }

    /**
     * Crée ou met à jour un contexte de mot.
     * Lie une instance de mot à une traduction spécifique et à son contexte de paragraphe.
     */
    @Transactional
    public WordContext createOrUpdateWordContext(UUID bookId, Integer pageNumber, String wordInstanceId,
                                                String wordTextInContext, UUID wordAnalysisId,
                                                WordTranslation translation, ParagraphContext paragraphContext) {
        log.debug("Creating or updating word context for instance: {} in book {} page {}", 
                wordInstanceId, bookId, pageNumber);

        // Vérifier si un contexte existe déjà pour cette instance
        Optional<WordContext> existingContext = wordContextRepository
                .findByBookIdAndPageNumberAndWordInstanceId(bookId, pageNumber, wordInstanceId);

        if (existingContext.isPresent()) {
            // Mettre à jour le contexte existant
            WordContext context = existingContext.get();
            context.setWordTranslationId(translation.getId());
            context.setWordAnalysisId(wordAnalysisId);
            context.setContextHash(paragraphContext.getContextHash());
            context.setWordTextInContext(wordTextInContext);
            
            WordContext updatedContext = wordContextRepository.save(context);
            log.info("Updated existing word context with ID: {}", updatedContext.getId());
            return updatedContext;
        } else {
            // Créer un nouveau contexte
            WordContext newContext = new WordContext(bookId, pageNumber, wordInstanceId,
                    wordTextInContext, wordAnalysisId, translation.getId(), paragraphContext.getContextHash());
            
            WordContext savedContext = wordContextRepository.save(newContext);
            log.info("Created new word context with ID: {}", savedContext.getId());
            return savedContext;
        }
    }

    /**
     * Trouve toutes les traductions disponibles pour une analyse de mot dans une langue donnée
     */
    @Transactional(readOnly = true)
    public List<WordTranslation> findAllTranslationsForWord(UUID wordAnalysisId, String languageCode) {
        return wordTranslationRepository.findByWordAnalysisIdAndLanguageCodeOrderByConfidenceScoreDesc(
                wordAnalysisId, languageCode);
    }

    /**
     * Trouve la meilleure traduction (plus haut score de confiance) pour une analyse et une langue
     */
    @Transactional(readOnly = true)
    public Optional<WordTranslation> findBestTranslation(UUID wordAnalysisId, String languageCode) {
        return wordTranslationRepository.findBestTranslation(wordAnalysisId, languageCode);
    }

    /**
     * Récupère un contexte de mot complet avec tous ses détails
     */
    @Transactional(readOnly = true)
    public Optional<WordContext> findWordContextWithDetails(UUID bookId, Integer pageNumber, String wordInstanceId) {
        return wordContextRepository.findWithDetailsBy(bookId, pageNumber, wordInstanceId);
    }

    /**
     * Calcule le hash SHA-256 d'un texte pour la déduplication
     */
    private String calculateSHA256Hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Cannot calculate hash for paragraph context", e);
        }
    }
}