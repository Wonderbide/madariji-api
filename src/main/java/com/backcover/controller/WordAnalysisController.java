// Dans com/backcover/controller/WordAnalysisController.java
package com.backcover.controller;

import com.backcover.config.LanguageConfig;
import com.backcover.dto.WordAnalysisDto;
import com.backcover.dto.WordAnalysisRequest;
import com.backcover.model.User;
import com.backcover.service.QuotaService;
import com.backcover.service.UserService;
import com.backcover.service.ia.DetailedWordAnalyzerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.backcover.exception.WordAnalysisException;

import java.io.IOException;

@RestController
@RequestMapping("/api/words")
public class WordAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(WordAnalysisController.class);

    private final DetailedWordAnalyzerService detailedWordAnalyzerService;
    private final UserService userService;
    private final LanguageConfig languageConfig;
    private final QuotaService quotaService;

    public WordAnalysisController(
            DetailedWordAnalyzerService detailedWordAnalyzerService, // Injection automatique via @Primary
            UserService userService,
            LanguageConfig languageConfig,
            QuotaService quotaService) {
        this.detailedWordAnalyzerService = detailedWordAnalyzerService;
        this.userService = userService;
        this.languageConfig = languageConfig;
        this.quotaService = quotaService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<WordAnalysisDto> analyzeWord(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @Valid @RequestBody WordAnalysisRequest request) {

        if (jwtPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        
        // Get email from JWT as primary identifier
        String email = jwtPrincipal.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }
        
        User currentUser = userService.findOrCreateUserByEmail(email);

        // Check quota for free users
        if (!quotaService.canConsultWord(currentUser)) {
            QuotaService.QuotaInfo quotaInfo = quotaService.getQuotaInfo(currentUser);
            log.info("User {} has reached daily quota: {}/{}", email, quotaInfo.used(), quotaInfo.used());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Limite quotidienne atteinte. Passez à Premium pour un accès illimité.");
        }

        // Validation et normalisation du targetLanguageCode
        String targetLanguageCode;
        try {
            targetLanguageCode = languageConfig.validateAndNormalize(request.getTargetLanguageCode());
            log.debug("Language validated and normalized: {} -> {}", request.getTargetLanguageCode(), targetLanguageCode);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid target language '{}': {}", request.getTargetLanguageCode(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Langue non supportée. Langues disponibles: " + languageConfig.getSupportedLanguageCodes());
        }

        // Nettoyer le wordText en supprimant les signes de ponctuation attachés
        String cleanedWordText = cleanWordText(request.getWordText());
        
        // Log pour déboguer le problème de découpage du mot
        log.info("Analyse de mot demandée - wordText original: '{}', wordText nettoyé: '{}', wordInstanceId: '{}', paragraphText: '{}'", 
                request.getWordText(), cleanedWordText, request.getWordInstanceId(), 
                request.getParagraphText() != null ? request.getParagraphText().substring(0, Math.min(50, request.getParagraphText().length())) + "..." : "null");
        
        try {
            WordAnalysisDto analysisResult = detailedWordAnalyzerService.analyzeWord(
                    cleanedWordText,
                    request.getParagraphText(),
                    request.getBookTitle(),
                    request.getBookId(),
                    request.getPageNumber(),
                    request.getWordInstanceId(),
                    targetLanguageCode
            );

            // Increment quota after successful analysis
            quotaService.incrementWordConsultation(currentUser);

            return ResponseEntity.ok(analysisResult);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid arguments for word analysis: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (WordAnalysisException e) {
            log.error("Word analysis failed for '{}': {} (type: {})",
                request.getWordText(), e.getMessage(), e.getErrorType());
            throw e; // GlobalExceptionHandler will handle this
        } catch (IOException e) {
            log.error("IO error during word analysis for '{}': {}", request.getWordText(), e.getMessage());
            throw new WordAnalysisException(WordAnalysisException.ErrorType.INTERNAL_ERROR, e);
        }
    }
    
    /**
     * Nettoie le texte du mot en supprimant les signes de ponctuation attachés
     * (virgules, points, guillemets, virgule arabe ، etc.)
     */
    private String cleanWordText(String wordText) {
        if (wordText == null || wordText.isBlank()) {
            return wordText;
        }
        
        // Supprimer les signes de ponctuation en début et fin de mot
        // Inclut la virgule arabe ، et autres signes de ponctuation
        // Garder les caractères arabes, les lettres latines, et les diacritiques
        return wordText.replaceAll("^[\\p{Punct}\\s،]+|[\\p{Punct}\\s،]+$", "").trim();
    }
}