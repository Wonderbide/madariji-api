package com.backcover.controller;

import com.backcover.config.LanguageConfig;
import com.backcover.dto.wordlist.AddWordToListRequestDto;
import com.backcover.dto.wordlist.UserWordListItemDto;
import com.backcover.dto.wordlist.UserWordListItemWithDetailsDto;
import com.backcover.dto.wordlist.UserWordListSummaryDto;
import com.backcover.service.UserWordListService;
import com.backcover.util.security.AuthenticationHelper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/wordlist")
public class UserWordListController {
    
    private static final Logger log = LoggerFactory.getLogger(UserWordListController.class);
    
    private final UserWordListService userWordListService;
    private final AuthenticationHelper authenticationHelper;
    private final LanguageConfig languageConfig;
    
    public UserWordListController(UserWordListService userWordListService, AuthenticationHelper authenticationHelper, LanguageConfig languageConfig) {
        this.userWordListService = userWordListService;
        this.authenticationHelper = authenticationHelper;
        this.languageConfig = languageConfig;
    }
    
    /**
     * Add a word to a book-specific list (creates list automatically if needed)
     * This is the main endpoint used when users click on words in books
     */
    @PostMapping("/items")
    public ResponseEntity<UserWordListItemDto> addWordToList(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @Valid @RequestBody AddWordToListRequestDto addRequest) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to add word to book-specific list for user: {}, book: {}", supabaseUserId, addRequest.getBookId());
        
        // Le comportement par défaut est maintenant de créer une liste spécifique au livre
        UserWordListItemDto addedItem = userWordListService.addWordToBookSpecificList(supabaseUserId, addRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }
    
    /**
     * Add a word to the user's default/language list (for future advanced features)
     * This endpoint is for when users want to create cross-book lists manually
     */
    @PostMapping("/default/items")
    public ResponseEntity<UserWordListItemDto> addWordToDefaultList(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @Valid @RequestBody AddWordToListRequestDto addRequest) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to add word to default/language list for user: {}", supabaseUserId);
        
        UserWordListItemDto addedItem = userWordListService.addWordToDefaultList(supabaseUserId, addRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }
    
    /**
     * Add a word to a book-specific list (alternative endpoint with explicit book ID)
     */
    @PostMapping("/book/{bookId}/items")
    public ResponseEntity<UserWordListItemDto> addWordToBookSpecificList(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @Valid @RequestBody AddWordToListRequestDto addRequest) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to add word to book-specific list for user: {}, book: {}", supabaseUserId, bookId);
        
        // Ensure the book ID in the request matches the path parameter
        if (!bookId.equals(addRequest.getBookId())) {
            log.warn("Book ID mismatch: path={}, request body={}", bookId, addRequest.getBookId());
            return ResponseEntity.badRequest().build();
        }
        
        UserWordListItemDto addedItem = userWordListService.addWordToBookSpecificList(supabaseUserId, addRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }
    
    /**
     * Get words from the user's default list
     * @deprecated This endpoint returns empty results since we use book-specific lists.
     * Use GET /api/user/wordlist/lists to get all lists, then 
     * GET /api/user/wordlist/book/{bookId}/items/details for specific list content.
     */
    @Deprecated
    @GetMapping("/items")
    public ResponseEntity<List<UserWordListItemDto>> getUserWordList(
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        log.warn("DEPRECATED: GET /api/user/wordlist/items called - this endpoint is deprecated");
        log.info("CONTROLLER: Returning empty list - use /api/user/wordlist/lists instead");
        
        // Return empty list with deprecation warning in logs
        return ResponseEntity.ok(List.of());
    }
    
    /**
     * Get words from the user's default list with word analysis details
     * @param targetLanguageCode Code de la langue pour les traductions (ex: 'fr', 'en', 'ar')
     */
    @GetMapping("/items/details")
    public ResponseEntity<List<UserWordListItemWithDetailsDto>> getUserWordListWithDetails(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(value = "targetLanguageCode", defaultValue = "fr") String targetLanguageCode) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to get word list with details for user: {} in language: {}", supabaseUserId, targetLanguageCode);
        
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        log.debug("Language normalized from '{}' to '{}'", targetLanguageCode, normalizedLanguage);
        
        List<UserWordListItemWithDetailsDto> wordListItems = userWordListService.getWordsFromDefaultListWithDetails(supabaseUserId, normalizedLanguage);
        return ResponseEntity.ok(wordListItems);
    }
    
    /**
     * Get inventory of word lists for the user, optionally filtered by language
     * @param targetLanguageCode Filter lists by target language (for word translations)
     * @param lang UI language for book title translation (defaults to 'ar')
     */
    @GetMapping("/lists")
    public ResponseEntity<List<UserWordListSummaryDto>> getUserWordLists(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(value = "targetLanguageCode", required = false) String targetLanguageCode,
            @RequestParam(value = "lang", defaultValue = "ar") String lang) {

        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to get word lists inventory for user: {} with language filter: {}, UI lang: {}", supabaseUserId, targetLanguageCode, lang);

        List<UserWordListSummaryDto> wordLists;
        if (targetLanguageCode != null && !targetLanguageCode.isBlank()) {
            // Valider et normaliser la langue
            String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
            log.debug("Language normalized from '{}' to '{}'", targetLanguageCode, normalizedLanguage);
            wordLists = userWordListService.getAllWordListsForUserInLanguage(supabaseUserId, normalizedLanguage, lang);
        } else {
            wordLists = userWordListService.getAllWordListsForUser(supabaseUserId, lang);
        }
        return ResponseEntity.ok(wordLists);
    }
    
    /**
     * Get words from a specific book for the user with word analysis details (from book-specific list in target language)
     * @param targetLanguageCode Code de la langue pour les traductions (ex: 'fr', 'en', 'ar')
     */
    @GetMapping("/book/{bookId}/items/details")
    public ResponseEntity<List<UserWordListItemWithDetailsDto>> getBookWordsWithDetails(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(value = "targetLanguageCode", defaultValue = "fr") String targetLanguageCode) {
        
        log.info("CONTROLLER: Starting getBookWordsWithDetails - bookId: {}, language: {}", bookId, targetLanguageCode);
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("CONTROLLER: Extracted supabaseUserId: {} for book: {}", supabaseUserId, bookId);
        
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        log.debug("Language normalized from '{}' to '{}'", targetLanguageCode, normalizedLanguage);
        
        try {
            List<UserWordListItemWithDetailsDto> wordItems = userWordListService.getWordsFromBookSpecificListWithDetails(supabaseUserId, bookId, normalizedLanguage);
            log.info("CONTROLLER: Successfully retrieved {} word items with details for book {} and user {} in language {}", 
                    wordItems.size(), bookId, supabaseUserId, normalizedLanguage);
            return ResponseEntity.ok(wordItems);
        } catch (Exception e) {
            log.error("CONTROLLER: Error getting words with details for book {} and user {} in language {}: {}", 
                    bookId, supabaseUserId, normalizedLanguage, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get words from a book-specific list with details
     * @param targetLanguageCode Code de la langue pour les traductions (ex: 'fr', 'en', 'ar')
     */
    @GetMapping("/book/{bookId}/list/items/details")
    public ResponseEntity<List<UserWordListItemWithDetailsDto>> getBookSpecificListWordsWithDetails(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(value = "targetLanguageCode", defaultValue = "fr") String targetLanguageCode) {
        
        log.info("CONTROLLER: Starting getBookSpecificListWordsWithDetails - bookId: {}, language: {}", bookId, targetLanguageCode);
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("CONTROLLER: Extracted supabaseUserId: {} for book-specific list: {}", supabaseUserId, bookId);
        
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        log.debug("Language normalized from '{}' to '{}'", targetLanguageCode, normalizedLanguage);
        
        try {
            List<UserWordListItemWithDetailsDto> wordItems = userWordListService.getWordsFromBookSpecificListWithDetails(supabaseUserId, bookId, normalizedLanguage);
            log.info("CONTROLLER: Successfully retrieved {} word items from book-specific list for book {} and user {} in language {}", 
                    wordItems.size(), bookId, supabaseUserId, normalizedLanguage);
            return ResponseEntity.ok(wordItems);
        } catch (Exception e) {
            log.error("CONTROLLER: Error getting words from book-specific list for book {} and user {} in language {}: {}", 
                    bookId, supabaseUserId, normalizedLanguage, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Link a word analysis to an existing word list item
     */
    @PutMapping("/items/{itemId}/analysis/{analysisId}")
    public ResponseEntity<UserWordListItemWithDetailsDto> linkAnalysisToItem(
            @PathVariable UUID itemId,
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        log.info("CONTROLLER: Starting linkAnalysisToItem - itemId: {}, analysisId: {}", itemId, analysisId);
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("CONTROLLER: Extracted supabaseUserId: {} for linking analysis {} to item {}", 
                supabaseUserId, analysisId, itemId);
        
        try {
            UserWordListItemWithDetailsDto result = userWordListService.linkAnalysisToWordListItem(supabaseUserId, itemId, analysisId);
            log.info("CONTROLLER: Successfully linked analysis {} to item {} for user {}", 
                    analysisId, itemId, supabaseUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("CONTROLLER: Error linking analysis {} to item {} for user {}: {}", 
                    analysisId, itemId, supabaseUserId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Remove a word from the user's list
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeWordFromList(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @PathVariable UUID itemId) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to remove word item {} from list for user: {}", itemId, supabaseUserId);
        
        boolean wasDeleted = userWordListService.removeWordFromList(supabaseUserId, itemId);
        
        if (wasDeleted) {
            return ResponseEntity.noContent().build(); // 204 - Successfully deleted
        } else {
            return ResponseEntity.notFound().build(); // 404 - Item not found or doesn't belong to user
        }
    }
    
    /**
     * OPTIMIZED: Remove a word from a specific list (more efficient)
     */
    @DeleteMapping("/lists/{listId}/items/{itemId}")
    public ResponseEntity<Void> removeWordFromSpecificList(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @PathVariable UUID listId,
            @PathVariable UUID itemId) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to remove word item {} from list {} for user: {}", itemId, listId, supabaseUserId);
        
        boolean wasDeleted = userWordListService.removeWordFromSpecificList(supabaseUserId, listId, itemId);
        
        if (wasDeleted) {
            return ResponseEntity.noContent().build(); // 204 - Successfully deleted
        } else {
            return ResponseEntity.notFound().build(); // 404 - Item not found or doesn't belong to user
        }
    }
}