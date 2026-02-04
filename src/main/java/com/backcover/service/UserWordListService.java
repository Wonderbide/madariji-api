package com.backcover.service;

import com.backcover.dto.ConsultedWordDetailsDto;
import com.backcover.dto.wordlist.AddWordToListRequestDto;
import com.backcover.dto.wordlist.UserWordListItemDto;
import com.backcover.dto.wordlist.UserWordListItemWithDetailsDto;
import com.backcover.dto.wordlist.UserWordListSummaryDto;
import com.backcover.model.*;
import com.backcover.model.user.UserWordList;
import com.backcover.model.user.UserWordListItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.backcover.repository.BookRepository;
import com.backcover.repository.BookMetadataTranslationRepository;
import com.backcover.repository.DictionaryWordRepository;
import com.backcover.repository.UserRepository;
import com.backcover.repository.UserWordListItemRepository;
import com.backcover.repository.UserWordListRepository;
import com.backcover.repository.WordAnalysisRepository;
import com.backcover.model.BookMetadataTranslation;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserWordListService {
    
    private static final Logger log = LoggerFactory.getLogger(UserWordListService.class);
    
    private final UserWordListRepository userWordListRepository;
    private final UserWordListItemRepository userWordListItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final WordAnalysisRepository wordAnalysisRepository;
    private final DictionaryWordRepository dictionaryWordRepository;
    private final TranslationContextService translationContextService;
    private final ObjectMapper objectMapper;
    private final BookMetadataTranslationRepository bookMetadataTranslationRepository;

    public UserWordListService(
            UserWordListRepository userWordListRepository,
            UserWordListItemRepository userWordListItemRepository,
            UserRepository userRepository,
            BookRepository bookRepository,
            WordAnalysisRepository wordAnalysisRepository,
            DictionaryWordRepository dictionaryWordRepository,
            TranslationContextService translationContextService,
            ObjectMapper objectMapper,
            BookMetadataTranslationRepository bookMetadataTranslationRepository) {
        this.userWordListRepository = userWordListRepository;
        this.userWordListItemRepository = userWordListItemRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.wordAnalysisRepository = wordAnalysisRepository;
        this.dictionaryWordRepository = dictionaryWordRepository;
        this.translationContextService = translationContextService;
        this.objectMapper = objectMapper;
        this.bookMetadataTranslationRepository = bookMetadataTranslationRepository;
    }
    
    /**
     * Find or create the default word list for a user
     */
    @Transactional
    /**
     * Find or create default word list for a user (backward compatibility)
     * @deprecated Use findOrCreateDefaultList(User, String) instead
     */
    @Deprecated
    public UserWordList findOrCreateDefaultList(User user) {
        return findOrCreateDefaultList(user, "fr"); // Default to French
    }
    
    /**
     * Find or create default word list for a user in a specific language
     */
    public UserWordList findOrCreateDefaultList(User user, String languageCode) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (languageCode == null || languageCode.isBlank()) {
            languageCode = "fr"; // Default language
        }
        
        final String finalLanguageCode = languageCode; // Make effectively final for lambda
        
        return userWordListRepository.findByUserIdAndLanguageCodeAndIsDefaultTrue(user.getId(), finalLanguageCode)
                .orElseGet(() -> {
                    log.info("Creating default word list for user ID: {}, Supabase ID: {}, language: {}", 
                            user.getId(), user.getSupabaseUserId(), finalLanguageCode);
                    
                    UserWordList defaultList = new UserWordList(user, 
                        "Default List (" + finalLanguageCode.toUpperCase() + ")", true, finalLanguageCode);
                    return userWordListRepository.save(defaultList);
                });
    }
    
    /**
     * Find or create book-specific word list for a user in a specific language
     */
    public UserWordList findOrCreateBookSpecificList(User user, Book book, String languageCode) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        if (languageCode == null || languageCode.isBlank()) {
            languageCode = "fr"; // Default language
        }
        
        final String finalLanguageCode = languageCode;
        
        return userWordListRepository.findByUserSupabaseUserIdAndLanguageCodeAndBookId(
                user.getSupabaseUserId(), finalLanguageCode, book.getId())
                .orElseGet(() -> {
                    log.info("Creating book-specific word list for user ID: {}, Supabase ID: {}, book ID: {}, language: {}", 
                            user.getId(), user.getSupabaseUserId(), book.getId(), finalLanguageCode);
                    
                    String listName = book.getTitle() + " (" + finalLanguageCode.toUpperCase() + ")";
                    UserWordList bookList = new UserWordList(user, listName, false, finalLanguageCode, book);
                    return userWordListRepository.save(bookList);
                });
    }
    
    /**
     * Add a word to a book-specific list (creates list if doesn't exist)
     */
    @Transactional
    public UserWordListItemDto addWordToBookSpecificList(String supabaseUserId, AddWordToListRequestDto requestDto) {
        return addWordToList(supabaseUserId, requestDto, true); // true = book-specific
    }
    
    /**
     * Add a word to the user's default list
     */
    @Transactional
    public UserWordListItemDto addWordToDefaultList(String supabaseUserId, AddWordToListRequestDto requestDto) {
        return addWordToList(supabaseUserId, requestDto, false); // false = default list
    }
    
    /**
     * Add a word to either a book-specific list or default list
     */
    public UserWordListItemDto addWordToList(String supabaseUserId, AddWordToListRequestDto requestDto, boolean useBookSpecificList) {
        // Find the user - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Find the book
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        
        // Check if user has access to the book if it's private
        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE) {
            boolean isOwner = book.getUser() != null && book.getUser().getId().equals(user.getId());
            if (!isOwner) {
                log.warn("User {} attempted to add word from private book {} they don't own", 
                        user.getId(), book.getId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book");
            }
        }
        
        // Find word analysis - either by provided ID or search for existing analysis
        WordAnalysis wordAnalysis = null;
        if (requestDto.getWordAnalysisId() != null) {
            // Use provided analysis ID
            wordAnalysis = wordAnalysisRepository.findById(requestDto.getWordAnalysisId()).orElse(null);
            log.debug("SERVICE: Found word analysis by provided ID: {} for word '{}'", 
                    requestDto.getWordAnalysisId(), requestDto.getWordText());
        } else {
            // Try to find existing analysis for this word text
            // Note: This assumes you have a method to find analysis by word text
            // You might need to implement this based on your WordAnalysis structure
            log.debug("SERVICE: No wordAnalysisId provided, attempting to find existing analysis for word '{}'", 
                    requestDto.getWordText());
            // TODO: Implement findByWordText or similar method if needed
            // wordAnalysis = wordAnalysisRepository.findByWordText(requestDto.getWordText()).orElse(null);
        }
        
        // Word analysis is required to determine language
        if (wordAnalysis == null) {
            log.error("SERVICE: No word analysis found for word '{}' - this should not happen in normal flow", requestDto.getWordText());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Word analysis is required to add word to list. Please analyze the word first.");
        }
        
        // Validate target language code
        String targetLanguageCode = requestDto.getTargetLanguageCode();
        if (targetLanguageCode == null || targetLanguageCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Target language code is required");
        }
        
        // Check that translation exists in the requested language
        Optional<WordTranslation> translationOpt = translationContextService
                .findBestTranslation(wordAnalysis.getId(), targetLanguageCode);
        
        if (translationOpt.isEmpty()) {
            log.error("SERVICE: Word '{}' has never been translated to language '{}'", 
                    requestDto.getWordText(), targetLanguageCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "WORD NEVER TRANSLATED");
        }
        
        log.info("SERVICE: Using target language '{}' for word '{}' (translation found)", 
                targetLanguageCode, requestDto.getWordText());
        
        // Choose list based on useBookSpecificList flag
        UserWordList targetList;
        if (useBookSpecificList) {
            targetList = findOrCreateBookSpecificList(user, book, targetLanguageCode);
            log.info("SERVICE: Using book-specific list '{}' for book '{}' and language '{}'", 
                    targetList.getListName(), book.getTitle(), targetLanguageCode);
        } else {
            targetList = findOrCreateDefaultList(user, targetLanguageCode);
            log.info("SERVICE: Using default list '{}' for language '{}'", 
                    targetList.getListName(), targetLanguageCode);
        }
        
        // Check if word already exists in the list
        if (userWordListItemRepository.existsByWordListIdAndBookIdAndWordInstanceId(
                targetList.getId(), book.getId(), requestDto.getWordInstanceId())) {
            log.info("Word instance {} from book {} already exists in list {} for user {}", 
                    requestDto.getWordInstanceId(), book.getId(), targetList.getId(), user.getId());
            
            // Return the existing item
            UserWordListItem existingItem = userWordListItemRepository
                    .findByWordListIdAndBookIdAndWordInstanceId(
                            targetList.getId(), book.getId(), requestDto.getWordInstanceId())
                    .orElseThrow(); // This should never happen due to the existsBy check above
            
            return UserWordListItemDto.fromEntity(existingItem);
        }
        
        log.info("SERVICE: Linking word '{}' to analysis ID: {}", requestDto.getWordText(), wordAnalysis.getId());
        
        // Create new word list item
        UserWordListItem newItem = new UserWordListItem(
                targetList,
                user,
                book,
                requestDto.getPageNumber(),
                requestDto.getWordInstanceId(),
                requestDto.getWordText(),
                wordAnalysis
        );
        
        // Save and return
        UserWordListItem savedItem = userWordListItemRepository.save(newItem);
        log.info("Added word '{}' to {} list '{}' for user {}", 
                requestDto.getWordText(), 
                useBookSpecificList ? "book-specific" : "default",
                targetList.getListName(),
                user.getId());
        
        return UserWordListItemDto.fromEntity(savedItem);
    }
    
    /**
     * Get words from the user's default list
     */
    @Transactional
    public List<UserWordListItemDto> getWordsFromDefaultList(String supabaseUserId) {
        log.info("SERVICE: Starting getWordsFromDefaultList - supabaseUserId: {}", supabaseUserId);
        
        // Find the user
        log.debug("SERVICE: Looking for user with email: {}", supabaseUserId);
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found with supabaseUserId: {}", supabaseUserId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        log.info("SERVICE: User found - userId: {}, supabaseUserId: {}", user.getId(), user.getSupabaseUserId());
        
        // Find default list (don't create it if it doesn't exist)
        log.debug("SERVICE: Looking for default word list for userId: {}", user.getId());
        Optional<UserWordList> defaultListOpt = userWordListRepository.findByUserIdAndIsDefaultTrue(user.getId());
        
        if (defaultListOpt.isEmpty()) {
            log.info("SERVICE: No default word list found for user {} - returning empty list", user.getId());
            return List.of();
        }
        
        UserWordList defaultList = defaultListOpt.get();
        log.info("SERVICE: Default word list found - listId: {}, listName: {}", 
                defaultList.getId(), defaultList.getListName());
        
        // Get items and map to DTOs (ordered by page, then chronologically desc)
        log.debug("SERVICE: Querying word list items for listId: {}", defaultList.getId());
        List<UserWordListItem> wordItems = userWordListItemRepository
                .findByWordListIdOrderByPageNumberAscAddedAtDesc(defaultList.getId());
        log.info("SERVICE: Found {} word items in default list for user {}", wordItems.size(), supabaseUserId);
        
        // Map to DTOs
        List<UserWordListItemDto> result = wordItems.stream()
                .map(item -> {
                    log.debug("SERVICE: Mapping word item - id: {}, wordText: {}, pageNumber: {}, bookId: {}", 
                            item.getId(), item.getWordText(), item.getPageNumber(), item.getBook().getId());
                    return UserWordListItemDto.fromEntity(item);
                })
                .toList();
        
        log.info("SERVICE: Returning {} word items from default list for user {}", result.size(), supabaseUserId);
        return result;
    }
    
    /**
     * Get words from the user's default list with word analysis details
     * @deprecated Use getWordsFromDefaultListWithDetails(String, String) instead
     */
    @Transactional
    @Deprecated
    public List<UserWordListItemWithDetailsDto> getWordsFromDefaultListWithDetails(String supabaseUserId) {
        // Default to French for backward compatibility
        return getWordsFromDefaultListWithDetails(supabaseUserId, "fr");
    }
    
    /**
     * Get words from the user's default list with word analysis details in a specific language
     * @param supabaseUserId ID utilisateur Supabase
     * @param targetLanguageCode Code de la langue pour les traductions
     */
    @Transactional
    public List<UserWordListItemWithDetailsDto> getWordsFromDefaultListWithDetails(String supabaseUserId, String targetLanguageCode) {
        log.info("SERVICE: Starting getWordsFromDefaultListWithDetails - supabaseUserId: {}, targetLanguageCode: {}", supabaseUserId, targetLanguageCode);
        
        // Find the user - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Find default list (don't create it if it doesn't exist)
        Optional<UserWordList> defaultListOpt = userWordListRepository.findByUserIdAndIsDefaultTrue(user.getId());
        
        if (defaultListOpt.isEmpty()) {
            log.info("No default word list found for user {}", user.getId());
            return List.of();
        }
        
        // Get items and map to DTOs with details in the target language
        List<UserWordListItem> wordItems = userWordListItemRepository
                .findByWordListIdOrderByPageNumberAscAddedAtDesc(defaultListOpt.get().getId());
        
        return wordItems.stream()
                .map(item -> buildUserWordListItemWithDetailsDto(item, targetLanguageCode))
                .toList();
    }
    
    /**
     * Get words from a book-specific list with details
     */
    @Transactional
    public List<UserWordListItemWithDetailsDto> getWordsFromBookSpecificListWithDetails(String supabaseUserId, UUID bookId, String targetLanguageCode) {
        log.info("SERVICE: Starting getWordsFromBookSpecificListWithDetails - supabaseUserId: {}, bookId: {}, targetLanguageCode: {}", 
                supabaseUserId, bookId, targetLanguageCode);
        
        // Find the user - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Find the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        
        // Check access to book if it's private
        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE) {
            boolean isOwner = book.getUser() != null && book.getUser().getId().equals(user.getId());
            if (!isOwner) {
                log.warn("User {} attempted to access words from private book {} they don't own", 
                        user.getId(), book.getId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book");
            }
        }
        
        // Find book-specific list (don't create if it doesn't exist)
        Optional<UserWordList> bookListOpt = userWordListRepository
                .findByUserSupabaseUserIdAndLanguageCodeAndBookId(supabaseUserId, targetLanguageCode, bookId);
        
        if (bookListOpt.isEmpty()) {
            log.info("No book-specific word list found for user {}, book {}, language {}", 
                    user.getId(), bookId, targetLanguageCode);
            return List.of();
        }
        
        // Get items and map to DTOs with details
        List<UserWordListItem> wordItems = userWordListItemRepository
                .findByWordListIdOrderByPageNumberAscAddedAtDesc(bookListOpt.get().getId());
        
        return wordItems.stream()
                .map(item -> buildUserWordListItemWithDetailsDto(item, targetLanguageCode))
                .toList();
    }
    
    /**
     * Get all word lists for a user (inventory) - only non-empty lists
     * @param supabaseUserId User ID
     * @param uiLang UI language for book title translation
     */
    @Transactional
    public List<UserWordListSummaryDto> getAllWordListsForUser(String supabaseUserId, String uiLang) {
        // Find the user - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Get all word lists for the user
        List<UserWordList> userWordLists = userWordListRepository.findByUserId(user.getId());

        // Map to DTOs with item counts and filter out empty lists
        return userWordLists.stream()
                .map(wordList -> {
                    long itemCount = userWordListItemRepository.countByWordListId(wordList.getId());
                    UserWordListSummaryDto dto = UserWordListSummaryDto.fromEntity(wordList, itemCount);
                    applyBookTitleTranslation(dto, wordList, uiLang);
                    return dto;
                })
                .filter(dto -> dto.getItemCount() > 0) // Only return lists with items
                .toList();
    }
    
    /**
     * Get word lists for a user filtered by language code - only non-empty lists
     * @param supabaseUserId User ID
     * @param languageCode Target language code for filtering word lists
     * @param uiLang UI language for book title translation
     */
    @Transactional
    public List<UserWordListSummaryDto> getAllWordListsForUserInLanguage(String supabaseUserId, String languageCode, String uiLang) {
        log.info("Getting word lists for user {} in language {}, UI lang: {}", supabaseUserId, languageCode, uiLang);

        // Find the user - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Get word lists for the user in specific language
        List<UserWordList> userWordLists = userWordListRepository.findByUserIdAndLanguageCode(user.getId(), languageCode);

        if (userWordLists.isEmpty()) {
            log.info("No word lists found for user {} in language {}", supabaseUserId, languageCode);
            return List.of();
        }

        // Map to DTOs with item counts and filter out empty lists
        return userWordLists.stream()
                .map(wordList -> {
                    long itemCount = userWordListItemRepository.countByWordListId(wordList.getId());
                    UserWordListSummaryDto dto = UserWordListSummaryDto.fromEntity(wordList, itemCount);
                    applyBookTitleTranslation(dto, wordList, uiLang);
                    return dto;
                })
                .filter(dto -> dto.getItemCount() > 0) // Only return lists with items
                .toList();
    }
    
    /**
     * Get words from a specific book for a user with word analysis details
     * @deprecated Use getWordsFromBookWithDetails(String, UUID, String) instead
     */
    @Transactional
    @Deprecated
    public List<UserWordListItemWithDetailsDto> getWordsFromBookWithDetails(String supabaseUserId, UUID bookId) {
        // Default to French for backward compatibility
        return getWordsFromBookWithDetails(supabaseUserId, bookId, "fr");
    }
    
    /**
     * Get words from a specific book for a user with word analysis details in a specific language
     * @param supabaseUserId ID utilisateur Supabase
     * @param bookId ID du livre
     * @param targetLanguageCode Code de la langue pour les traductions
     */
    @Transactional
    public List<UserWordListItemWithDetailsDto> getWordsFromBookWithDetails(String supabaseUserId, UUID bookId, String targetLanguageCode) {
        log.info("SERVICE: Starting getWordsFromBookWithDetails - supabaseUserId: {}, bookId: {}, targetLanguageCode: {}", supabaseUserId, bookId, targetLanguageCode);
        
        // Find the user first to validate access
        log.debug("SERVICE: Looking for user with email: {}", supabaseUserId);
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found with supabaseUserId: {}", supabaseUserId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        log.info("SERVICE: User found - userId: {}, supabaseUserId: {}", user.getId(), user.getSupabaseUserId());
        
        // Verify the book exists and user has access to it
        log.debug("SERVICE: Looking for book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("SERVICE: Book not found with id: {}", bookId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                });
        log.info("SERVICE: Book found - bookId: {}, title: {}, visibility: {}, ownerId: {}", 
                book.getId(), book.getTitle(), book.getVisibilityStatus(), 
                book.getUser() != null ? book.getUser().getId() : "null");
        
        // Check if user has access to the book if it's private
        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE) {
            boolean isOwner = book.getUser() != null && book.getUser().getId().equals(user.getId());
            log.info("SERVICE: Private book access check - isOwner: {}, bookOwnerId: {}, requestingUserId: {}", 
                    isOwner, book.getUser() != null ? book.getUser().getId() : "null", user.getId());
            if (!isOwner) {
                log.warn("SERVICE: User {} attempted to access words from private book {} they don't own", 
                        user.getId(), book.getId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book");
            }
        } else {
            log.info("SERVICE: Book is public, access granted");
        }
        
        // Get all word items for this book and user, ordered by page then chronologically desc
        log.debug("SERVICE: Querying word list items for supabaseUserId: {} and bookId: {}", supabaseUserId, bookId);
        List<UserWordListItem> wordItems = userWordListItemRepository
                .findByUserSupabaseUserIdAndBookIdOrderByPageNumberAscAddedAtDesc(supabaseUserId, bookId);
        log.info("SERVICE: Found {} word items for book {} and user {}", wordItems.size(), bookId, supabaseUserId);
        
        // Map to DTOs with details in the target language
        List<UserWordListItemWithDetailsDto> result = wordItems.stream()
                .map(item -> {
                    log.debug("SERVICE: Mapping word item - id: {}, wordText: {}, pageNumber: {}, hasAnalysis: {}", 
                            item.getId(), item.getWordText(), item.getPageNumber(), 
                            item.getWordAnalysis() != null);
                    return buildUserWordListItemWithDetailsDto(item, targetLanguageCode);
                })
                .toList();
        
        log.info("SERVICE: Returning {} word items with details for book {} and user {} in language {}", 
                result.size(), bookId, supabaseUserId, targetLanguageCode);
        return result;
    }
    
    /**
     * Link a word analysis to an existing word list item
     */
    @Transactional
    public UserWordListItemWithDetailsDto linkAnalysisToWordListItem(String supabaseUserId, UUID itemId, UUID analysisId) {
        log.info("SERVICE: Starting linkAnalysisToWordListItem - supabaseUserId: {}, itemId: {}, analysisId: {}", 
                supabaseUserId, itemId, analysisId);
        
        // Find the user first to validate access
        log.debug("SERVICE: Looking for user with email: {}", supabaseUserId);
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found with supabaseUserId: {}", supabaseUserId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        log.info("SERVICE: User found - userId: {}, supabaseUserId: {}", user.getId(), user.getSupabaseUserId());
        
        // Find the word list item and verify ownership
        log.debug("SERVICE: Looking for word list item with id: {}", itemId);
        UserWordListItem wordListItem = userWordListItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("SERVICE: Word list item not found with id: {}", itemId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Word list item not found");
                });
        
        // Verify the item belongs to the user
        if (!wordListItem.getUser().getSupabaseUserId().equals(supabaseUserId)) {
            log.warn("SERVICE: User {} attempted to modify word list item {} that doesn't belong to them", 
                    supabaseUserId, itemId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this word list item");
        }
        
        log.info("SERVICE: Word list item found - itemId: {}, wordText: {}, currentAnalysisId: {}", 
                wordListItem.getId(), wordListItem.getWordText(), 
                wordListItem.getWordAnalysis() != null ? wordListItem.getWordAnalysis().getId() : "null");
        
        // Find the word analysis
        log.debug("SERVICE: Looking for word analysis with id: {}", analysisId);
        WordAnalysis wordAnalysis = wordAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> {
                    log.error("SERVICE: Word analysis not found with id: {}", analysisId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Word analysis not found");
                });
        log.info("SERVICE: Word analysis found - analysisId: {}, source: {}", 
                wordAnalysis.getId(), wordAnalysis.getSource());
        
        // Link the analysis to the word list item
        UUID previousAnalysisId = wordListItem.getWordAnalysis() != null ? wordListItem.getWordAnalysis().getId() : null;
        wordListItem.setWordAnalysis(wordAnalysis);
        
        // Save the updated item
        UserWordListItem savedItem = userWordListItemRepository.save(wordListItem);
        log.info("SERVICE: Successfully linked analysis {} to word list item {} (previous analysis: {})", 
                analysisId, itemId, previousAnalysisId);
        
        return UserWordListItemWithDetailsDto.fromEntity(savedItem);
    }
    
    /**
     * Remove a word from a user's list
     */
    @Transactional
    public boolean removeWordFromList(String supabaseUserId, UUID listItemId) {
        int deletedCount = userWordListItemRepository.deleteByIdAndWordListUserSupabaseUserId(listItemId, supabaseUserId);
        
        if (deletedCount > 0) {
            log.info("Removed word list item {} for user with Supabase ID: {}", listItemId, supabaseUserId);
            return true;
        } else {
            log.warn("Word list item {} not found or does not belong to user with Supabase ID: {}", listItemId, supabaseUserId);
            return false;
        }
    }
    
    /**
     * OPTIMIZED: Remove word from list with separate security check
     * More efficient for high-volume operations
     */
    @Transactional
    public boolean removeWordFromListOptimized(String supabaseUserId, UUID listItemId) {
        // 1. Quick security check - single SELECT with JOIN
        Optional<UserWordListItem> itemOpt = userWordListItemRepository.findByIdAndUserSupabaseId(listItemId, supabaseUserId);
        
        if (itemOpt.isEmpty()) {
            log.warn("Word list item {} not found or doesn't belong to user {}", listItemId, supabaseUserId);
            return false;
        }
        
        // 2. Simple DELETE by ID only - no JOIN needed
        int deletedCount = userWordListItemRepository.deleteByItemId(listItemId);
        
        if (deletedCount > 0) {
            log.info("Optimized removal of word list item {} for user {}", listItemId, supabaseUserId);
            return true;
        } else {
            log.error("Failed to delete word list item {} after successful verification", listItemId);
            return false;
        }
    }
    
    /**
     * Remove a word from a specific list (most efficient)
     * Uses both listId and itemId for direct deletion without heavy JOINs
     */
    @Transactional
    public boolean removeWordFromSpecificList(String supabaseUserId, UUID listId, UUID itemId) {
        log.info("Removing word item {} from list {} for user {}", itemId, listId, supabaseUserId);
        
        // Direct deletion with listId and itemId - most efficient approach
        int deletedCount = userWordListItemRepository.deleteByIdAndWordListIdAndWordListUserSupabaseUserId(
            itemId, listId, supabaseUserId);
        
        if (deletedCount > 0) {
            log.info("Successfully removed word item {} from list {} for user {}", itemId, listId, supabaseUserId);
            return true;
        } else {
            log.warn("Word item {} not found in list {} or doesn't belong to user {}", itemId, listId, supabaseUserId);
            return false;
        }
    }
    
    /**
     * Récupère tous les mots consultés par un utilisateur pour un livre donné,
     * avec leurs détails d'analyse complets et leurs traductions dans la langue spécifiée.
     */
    @Transactional
    public List<ConsultedWordDetailsDto> getConsultedWordsWithDetails(String supabaseUserId, UUID bookId, String targetLanguageCode) {
        log.info("SERVICE: Starting getConsultedWordsWithDetails - supabaseUserId: {}, bookId: {}, targetLanguageCode: {}", 
                supabaseUserId, bookId, targetLanguageCode);

        // Normaliser la langue cible
        if (targetLanguageCode == null || targetLanguageCode.isBlank()) {
            targetLanguageCode = "fr"; // Langue par défaut
        }
        final String finalTargetLanguageCode = targetLanguageCode;

        // Vérifier que l'utilisateur existe - supabaseUserId now contains email
        User user = userRepository.findByEmail(supabaseUserId)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found for email: {}", supabaseUserId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });
        log.info("SERVICE: User found - userId: {}, supabaseUserId: {}", user.getId(), supabaseUserId);

        // Vérifier l'accès au livre
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("SERVICE: Book not found - bookId: {}", bookId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                });
        log.info("SERVICE: Book found - bookId: {}, title: {}, visibility: {}, ownerId: {}", 
                book.getId(), book.getTitle(), book.getVisibilityStatus(), book.getUser().getId());

        // Vérifier les permissions d'accès au livre
        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE && !book.getUser().getId().equals(user.getId())) {
            log.warn("SERVICE: Access denied to private book {} for user {}", bookId, user.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book");
        } else if (book.getVisibilityStatus() == BookVisibilityStatus.PUBLIC) {
            log.info("SERVICE: Book is public, access granted");
        } else {
            log.info("SERVICE: User is book owner, access granted");
        }

        // Récupérer les mots consultés pour ce livre ET pour les listes dans la langue demandée
        List<UserWordListItem> consultedWords = userWordListItemRepository
                .findByUserSupabaseUserIdAndBookIdAndListLanguageOrderByPageNumberAscAddedAtDesc(supabaseUserId, bookId, finalTargetLanguageCode);
        log.info("SERVICE: Found {} consulted words for book {} and user {} in lists with language {}", 
                consultedWords.size(), bookId, supabaseUserId, finalTargetLanguageCode);

        // Transformer chaque mot consulté en DTO avec détails complets
        List<ConsultedWordDetailsDto> result = consultedWords.stream().map(wordItem -> {
            try {
                return buildConsultedWordDetailsDto(wordItem, finalTargetLanguageCode);
            } catch (Exception e) {
                log.error("Failed to build details for word item {}: {}", wordItem.getId(), e.getMessage(), e);
                return null; // Skip this item in case of error
            }
        }).filter(dto -> dto != null).toList();

        log.info("SERVICE: Returning {} consulted words with details for book {} and user {}", 
                result.size(), bookId, supabaseUserId);
        return result;
    }

    /**
     * Construit un ConsultedWordDetailsDto à partir d'un UserWordListItem
     */
    private ConsultedWordDetailsDto buildConsultedWordDetailsDto(UserWordListItem wordItem, String targetLanguageCode) {
        ConsultedWordDetailsDto dto = new ConsultedWordDetailsDto();

        // Informations de base du mot consulté
        dto.setWordListItemId(wordItem.getId()); // ID pour suppression
        dto.setWordInstanceId(wordItem.getWordInstanceId());
        dto.setWordTextInContext(wordItem.getWordText());
        dto.setPageNumber(wordItem.getPageNumber());
        dto.setConsultedAt(wordItem.getAddedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());

        // Si le mot a une analyse liée
        WordAnalysis wordAnalysis = wordItem.getWordAnalysis();
        if (wordAnalysis != null) {
            dto.setWordAnalysisId(wordAnalysis.getId());
            dto.setAnalysisSource(wordAnalysis.getSource());

            // Parser les données d'analyse JSON
            try {
                if (wordAnalysis.getAnalysisData() != null && !wordAnalysis.getAnalysisData().isBlank()) {
                    JsonNode analysisJson = objectMapper.readTree(wordAnalysis.getAnalysisData());
                    dto.setWordType(analysisJson.path("type").asText(null));
                    dto.setRoot(analysisJson.path("root").asText(null));
                    dto.setCanonicalForm(analysisJson.path("canonical_form_from_llm").asText(null));
                    dto.setAnalysisDetails(analysisJson.path("details"));
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse analysis data for word analysis {}: {}", wordAnalysis.getId(), e.getMessage());
            }

            // Récupérer la traduction dans la langue cible
            Optional<WordTranslation> translationOpt = translationContextService
                    .findBestTranslation(wordAnalysis.getId(), targetLanguageCode);
            
            if (translationOpt.isPresent()) {
                WordTranslation translation = translationOpt.get();
                dto.setWordTranslationId(translation.getId());
                dto.setTranslationText(translation.getTranslationText());
                dto.setTranslationLanguageCode(translation.getLanguageCode());
                dto.setTranslationConfidenceScore(translation.getConfidenceScore());
                dto.setTranslationSource(translation.getSource());
            } else {
                log.info("No translation found for word analysis {} in language {}", 
                        wordAnalysis.getId(), targetLanguageCode);
            }

            // Récupérer le contexte du paragraphe via WordContext
            Optional<WordContext> contextOpt = translationContextService
                    .findWordContextWithDetails(wordItem.getBook().getId(), wordItem.getPageNumber(), wordItem.getWordInstanceId());
            
            if (contextOpt.isPresent()) {
                WordContext context = contextOpt.get();
                dto.setWordContextId(context.getId());
                
                // Récupérer le texte du paragraphe
                if (context.getParagraphContext() != null) {
                    dto.setParagraphText(context.getParagraphContext().getParagraphText());
                }
            }
        }

        return dto;
    }
    
    /**
     * Construit un UserWordListItemWithDetailsDto à partir d'un UserWordListItem
     * avec traduction dans la langue spécifiée
     */
    private UserWordListItemWithDetailsDto buildUserWordListItemWithDetailsDto(UserWordListItem item, String targetLanguageCode) {
        if (item == null) {
            return null;
        }
        
        // Si pas d'analyse, utiliser la méthode standard
        if (item.getWordAnalysis() == null) {
            log.debug("No word analysis for item {}, using standard DTO conversion", item.getId());
            return UserWordListItemWithDetailsDto.fromEntity(item);
        }
        
        try {
            // Récupérer la traduction dans la langue cible
            WordAnalysis analysis = item.getWordAnalysis();
            Optional<WordTranslation> translationOpt = translationContextService.findBestTranslation(
                analysis.getId(), targetLanguageCode);
            
            String translationText = translationOpt.map(WordTranslation::getTranslationText).orElse(null);
            
            log.debug("Found translation for word '{}' in language '{}': '{}'", 
                item.getWordText(), targetLanguageCode, translationText);
            
            // Utiliser la méthode fromEntity qui parse automatiquement le JSON
            // Cela nous donne la structure propre au lieu du JSON string
            UserWordListItemWithDetailsDto dto = UserWordListItemWithDetailsDto.fromEntity(item, targetLanguageCode);
            
            // Si on a une traduction spécifique, on peut l'injecter dans analysisDetails
            if (dto.getAnalysisDetails() != null && translationText != null) {
                dto.getAnalysisDetails().setTranslation(translationText);
            }
            
            return dto;
            
        } catch (Exception e) {
            log.warn("Error building DTO with translation for item {} in language {}: {}",
                item.getId(), targetLanguageCode, e.getMessage());
            // Fallback vers la méthode standard
            return UserWordListItemWithDetailsDto.fromEntity(item);
        }
    }

    /**
     * Apply book title translation to a word list summary DTO
     * @param dto The DTO to update
     * @param wordList The word list entity
     * @param uiLang The UI language for translation
     */
    private void applyBookTitleTranslation(UserWordListSummaryDto dto, UserWordList wordList, String uiLang) {
        if (wordList.getBook() == null || "ar".equals(uiLang)) {
            // No book or Arabic is the default, no translation needed
            return;
        }

        Optional<BookMetadataTranslation> translation = bookMetadataTranslationRepository
                .findByBookIdAndLanguageCode(wordList.getBook().getId(), uiLang);

        if (translation.isPresent() && translation.get().getTitle() != null) {
            String translatedTitle = translation.get().getTitle();
            dto.setBookTitle(translatedTitle);
            // Also update listName to use translated title
            dto.setListName(translatedTitle + " (" + wordList.getLanguageCode().toUpperCase() + ")");
            log.debug("Applied book title translation for list {}: {} -> {}",
                    dto.getId(), wordList.getBook().getTitle(), translatedTitle);
        }
    }

}