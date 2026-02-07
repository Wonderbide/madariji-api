// src/main/java/com/backcover/controller/BookController.java
package com.backcover.controller;

import com.backcover.dto.book.BookSummaryDto;
import com.backcover.dto.book.BookDetailDto;
import com.backcover.dto.BookProgressDto;
import com.backcover.model.Book;
import com.backcover.model.BookVisibilityStatus;
import com.backcover.model.User;
import com.backcover.model.BookStatus;
import com.backcover.service.UserService;
import com.backcover.service.ReadingActivityService;
import com.backcover.service.BookCoverService;
import com.backcover.repository.BookRepository;
import com.backcover.repository.BookMetadataTranslationRepository;
import com.backcover.model.BookMetadataTranslation;
import com.backcover.util.security.AuthenticationHelper; // <<< IMPORT DU HELPER
import com.backcover.service.storage.R2StorageService;
import com.backcover.service.AsyncEnrichmentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Lazy
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookMetadataTranslationRepository translationRepository;
    private final ReadingActivityService readingActivityService;
    private final AuthenticationHelper authenticationHelper;
    private final R2StorageService r2StorageService;
    private final BookCoverService bookCoverService;
    private final AsyncEnrichmentService asyncEnrichmentService;

    @Autowired
    public BookController(UserService userService,
                          BookRepository bookRepository,
                          BookMetadataTranslationRepository translationRepository,
                          ReadingActivityService readingActivityService,
                          AuthenticationHelper authenticationHelper,
                          @Qualifier("mainR2Storage") R2StorageService r2StorageService,
                          BookCoverService bookCoverService,
                          AsyncEnrichmentService asyncEnrichmentService) {
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.translationRepository = translationRepository;
        this.readingActivityService = readingActivityService;
        this.authenticationHelper = authenticationHelper;
        this.r2StorageService = r2StorageService;
        this.bookCoverService = bookCoverService;
        this.asyncEnrichmentService = asyncEnrichmentService;
    }

    @GetMapping
    public ResponseEntity<List<BookSummaryDto>> getUserBooks(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(defaultValue = "ar") String lang) {
        User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);

        // 1. Récupérer les livres appartenant à l'utilisateur
        List<Book> ownedBooks = bookRepository.findByUser(currentUser);

        // 2. Récupérer tous les livres publics
        List<Book> publicBooks = bookRepository.findByVisibilityStatus(BookVisibilityStatus.PUBLIC);

        // 3. Fusionner les listes et supprimer les doublons (les livres de l'utilisateur qui sont aussi publics)
        // Un HashSet est efficace pour gérer l'unicité basée sur la méthode equals/hashCode de l'entité Book (qui doit être basée sur l'ID)
        Set<Book> accessibleBooksSet = new HashSet<>(ownedBooks);
        accessibleBooksSet.addAll(publicBooks); // Ajoute les livres publics, les doublons sont ignorés par le Set

        List<Book> accessibleBooks = new ArrayList<>(accessibleBooksSet);

        // Optionnel: Trier la liste fusionnée si nécessaire (par exemple, par date d'upload)
        // accessibleBooks.sort(Comparator.comparing(Book::getUploadedAt).reversed());

        List<BookSummaryDto> bookSummaries = accessibleBooks.stream()
                .map(book -> {
                    // Récupérer la progression de lecture pour ce livre et cet utilisateur
                    Optional<Integer> lastReadPage = readingActivityService.getLastReadPage(currentUser.getId(), book.getId());
                    boolean hasBeenRead = lastReadPage.isPresent();

                    BookSummaryDto dto = new BookSummaryDto(
                            book.getId(),
                            book.getTitle(),
                            book.getStatus(),
                            book.getUploadedAt(),
                            book.getProcessingDetails(),
                            String.format("/api/books/%s/cover", book.getId().toString()),
                            lastReadPage.orElse(null),
                            book.getTotalPages(),
                            hasBeenRead
                    );
                    dto.setAuthorName(book.getAuthorName());
                    dto.setGenre(book.getGenre());
                    dto.setDescription(book.getDescription());
                    dto.setPublishedDateText(book.getPublishedDateText());

                    // Apply translation if not Arabic
                    applyTranslation(dto, book, lang);

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookSummaries);
    }

    @GetMapping("/owned")
    public ResponseEntity<List<BookSummaryDto>> getOwnedBooks(
            @AuthenticationPrincipal Jwt jwtPrincipal,
            @RequestParam(defaultValue = "ar") String lang) {
        User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);

        // Récupérer uniquement les livres appartenant à l'utilisateur
        List<Book> ownedBooks = bookRepository.findByUser(currentUser);

        List<BookSummaryDto> bookSummaries = ownedBooks.stream()
                .map(book -> {
                    // Récupérer la progression de lecture pour ce livre et cet utilisateur
                    Optional<Integer> lastReadPage = readingActivityService.getLastReadPage(currentUser.getId(), book.getId());
                    boolean hasBeenRead = lastReadPage.isPresent();

                    BookSummaryDto dto = new BookSummaryDto(
                            book.getId(),
                            book.getTitle(),
                            book.getStatus(),
                            book.getUploadedAt(),
                            book.getProcessingDetails(),
                            String.format("/api/books/%s/cover", book.getId().toString()),
                            lastReadPage.orElse(null),
                            book.getTotalPages(),
                            hasBeenRead
                    );
                    dto.setAuthorName(book.getAuthorName());
                    dto.setGenre(book.getGenre());
                    dto.setDescription(book.getDescription());
                    dto.setPublishedDateText(book.getPublishedDateText());

                    // Apply translation if not Arabic
                    applyTranslation(dto, book, lang);

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookSummaries);
    }
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailDto> getBookDetails(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        User currentUser = null;
        try {
            currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);
        } catch (Exception e) {
            log.debug("No authenticated user found for book details request: {}", e.getMessage());
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        boolean isOwner = currentUser != null && book.getUser() != null && book.getUser().getId().equals(currentUser.getId());

        // <<< CETTE CONDITION EST CORRECTE >>>
        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE && !isOwner) {
            log.warn("Access denied to private book {} for user (Supabase ID: {}, Local User Found: {})", bookId, (jwtPrincipal != null ? jwtPrincipal.getSubject() : "anonymous"), (currentUser != null));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book.");
        }

        Integer lastReadPage = null;
        if (currentUser != null) {
            UUID internalUserId = currentUser.getId();
            lastReadPage = readingActivityService.getLastReadPage(internalUserId, bookId).orElse(null);
        }

        BookDetailDto bookDetails = new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getStatus(),
                book.getUploadedAt(),
                book.getProcessingDetails(),
                book.getVisionOperationName(),
                book.getFinalContentPath(),
                lastReadPage
        );

        return ResponseEntity.ok(bookDetails);
    }

    @GetMapping("/{bookId}/progress")
    public ResponseEntity<BookProgressDto> getBookProgress(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        User currentUser = null;
        try {
            currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);
        } catch (Exception e) {
            log.debug("No authenticated user found for book progress request: {}", e.getMessage());
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        boolean isOwner = currentUser != null && book.getUser() != null && book.getUser().getId().equals(currentUser.getId());

        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE && !isOwner) {
            log.warn("Access denied to private book progress {} for user (Supabase ID: {})", 
                     bookId, (jwtPrincipal != null ? jwtPrincipal.getSubject() : "anonymous"));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book progress.");
        }

        // Construire le DTO de progression
        String currentStep = determineCurrentStep(book.getStatus());
        Integer currentPage = book.getLastSuccessfullyProcessedPageIndex() != null 
                ? book.getLastSuccessfullyProcessedPageIndex() + 1 
                : 0;

        BookProgressDto progressDto = new BookProgressDto(
                currentPage,
                book.getTotalPages(),
                book.getStatus(),
                currentStep
        );
        progressDto.setProcessingDetails(book.getProcessingDetails());

        log.debug("Progress requested for book {}: {}/{} pages ({}%)", 
                  bookId, currentPage, book.getTotalPages(), progressDto.getProgressPercentage());

        return ResponseEntity.ok(progressDto);
    }

    private String determineCurrentStep(BookStatus status) {
        return switch (status) {
            case PENDING -> "UPLOAD_COMPLETE";
            case TEXT_EXTRACTION_IN_PROGRESS -> "OCR_PROCESSING";
            case PROCESSING_OCR_RESULTS -> "OCR_RESULTS_PROCESSING";
            case AWAITING_ENRICHMENT -> "AWAITING_ENRICHMENT";
            case ENRICHMENT_IN_PROGRESS, PARTIALLY_ENRICHED -> "STRUCTURING";
            case COMPLETED -> "COMPLETED";
            case FAILED -> "FAILED";
            default -> "UNKNOWN";
        };
    }

// Dans com/backcover/controller/BookController.java

    @GetMapping("/{bookId}/structure")
    public ResponseEntity<String> getBookStructure(@PathVariable UUID bookId, @AuthenticationPrincipal Jwt jwtPrincipal) {
        // 1. Trouver le livre par son ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        // 2. Vérifier les droits d'accès
        User currentUser = null;
        try {
            currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);
        } catch (Exception e) {
            log.debug("No authenticated user found for book structure request: {}", e.getMessage());
        }

        boolean isOwner = currentUser != null && book.getUser() != null && book.getUser().getId().equals(currentUser.getId());

        if (book.getVisibilityStatus() == BookVisibilityStatus.PRIVATE) {
            if (!isOwner) {
                // Livre privé, et l'utilisateur n'est pas le propriétaire (ou n'est pas authentifié/trouvé)
                log.warn("Access denied to private book structure {} for user (Supabase ID: {}, Local User Found: {})",
                        bookId, (jwtPrincipal != null ? jwtPrincipal.getSubject() : "anonymous"), (currentUser != null));
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this book's structure.");
            }
            // Si c'est le propriétaire d'un livre privé, l'accès est autorisé
        }
        // Si le livre est PUBLIC, l'accès est autorisé pour tout le monde (authentifié ou non)

        // 3. Récupérer la structure depuis R2
        String r2StructuredKey = "batch/structured/" + bookId + "-structured.json";
        log.debug("Fetching structured content from R2: {}", r2StructuredKey);

        Optional<byte[]> r2Content = r2StorageService.downloadFile(r2StructuredKey);

        if (r2Content.isPresent()) {
            log.info("Structured content found in R2 for bookId: {}", bookId);
            String bookStructure = new String(r2Content.get(), java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok(bookStructure);
        }

        log.info("Structure not found in R2 for bookId: {}", bookId);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book structure not yet available.");
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('PRO', 'ADMIN')")
    public ResponseEntity<String> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "visibility", defaultValue = "PRIVATE") String visibilityString,
            @AuthenticationPrincipal Jwt jwtPrincipal
    ) {
        User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);

        // Titre = nom du fichier sans extension (sera remplacé par l'extraction async)
        String originalFilename = file.getOriginalFilename();
        String title = (originalFilename != null)
            ? originalFilename.replaceAll("\\.pdf$", "")
            : "Untitled";

        log.info("Upload pour le livre '{}' par l'utilisateur local ID: {}, Supabase ID: {}", title, currentUser.getId(), currentUser.getSupabaseUserId());

        UUID bookId = UUID.randomUUID();
        String coverPath = null;
        byte[] pdfBytes = null;
        
        try {
            pdfBytes = file.getBytes();
            // Générer et sauvegarder la couverture dans R2
            coverPath = bookCoverService.generateAndSaveCover(pdfBytes, bookId);
            if (coverPath != null) {
                log.info("Couverture générée et sauvegardée dans R2: {}", coverPath);
            } else {
                log.warn("La génération de la couverture a échoué pour le livre {}", bookId);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du PDF pour le livre {}: {}", bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la lecture du fichier PDF.");
        }

        Book book = new Book();
        book.setId(bookId);
        book.setTitle(title); // Nom du fichier, sera remplacé par l'extraction async

        book.setCoverImagePath(coverPath); // Chemin R2 de la couverture
        book.setStatus(BookStatus.PENDING);
        book.setUploadedAt(Instant.now());
        book.setUser(currentUser);

        try {
            BookVisibilityStatus visibilityStatus = BookVisibilityStatus.valueOf(visibilityString.toUpperCase());
            book.setVisibilityStatus(visibilityStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Valeur de visibilité invalide reçue: '{}'. Défaut à PRIVATE.", visibilityString);
            book.setVisibilityStatus(BookVisibilityStatus.PRIVATE);
        }

        // Pour l'instant, pas de source "officielle" de métadonnées
        // book.setIsMetadataVerified(false); // Si vous aviez ajouté ce champ
        // book.setMetadataSource("USER_INPUT"); // Si vous aviez ajouté ce champ

        Book savedBook = bookRepository.save(book);

        // Upload du PDF vers R2 pour le module batch (réutilise les bytes déjà lus)
        String r2Key = "books/raw/" + bookId + ".pdf";
        try {
            if (pdfBytes == null) {
                pdfBytes = file.getBytes();
            }
            Map<String, String> metadata = new HashMap<>();
            metadata.put("content-type", "application/pdf");
            metadata.put("book-id", bookId.toString());
            
            // Nettoyer le titre pour éviter les problèmes de signature S3
            // Les caractères non-ASCII dans les metadata peuvent casser la signature AWS
            if (title != null) {
                // Remplacer les caractères problématiques par des alternatives sûres
                String cleanTitle = title.replaceAll("[^\\x00-\\x7F]", "_") // Remplacer non-ASCII
                                        .replaceAll("[\\p{Cntrl}]", "") // Enlever caractères de contrôle
                                        .trim();
                metadata.put("title", cleanTitle);
                if (!cleanTitle.equals(title)) {
                    log.warn("Title contained special characters that were sanitized. Original: '{}', Clean: '{}'", 
                            title, cleanTitle);
                }
            }
            
            // Nettoyer l'ID utilisateur au cas où
            String userId = currentUser.getSupabaseUserId();
            if (userId != null) {
                String cleanUserId = userId.replaceAll("[^\\x00-\\x7F]", "_").trim();
                metadata.put("uploaded-by", cleanUserId);
            }
            
            log.debug("Uploading to R2 with metadata: {}", metadata);
            r2StorageService.uploadFile(r2Key, pdfBytes, metadata);
            log.info("PDF uploaded to R2: {} ({} KB)", r2Key, pdfBytes.length / 1024);
        } catch (Exception e) {
            // CRITIQUE: Si R2 échoue, le batch processor ne pourra pas traiter le livre
            log.error("Failed to upload PDF to R2 for book {} - Batch processing will NOT occur: {}",
                    bookId, e.getMessage(), e);

            // Marquer le livre comme FAILED car le batch processor ne le verra pas
            savedBook.setStatus(BookStatus.FAILED);
            savedBook.setProcessingDetails("R2 upload failed: " + e.getMessage());
            bookRepository.save(savedBook);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload PDF for processing. Please try again.");
        }

        // ============================================================
        // ENRICHISSEMENT v2 ASYNCHRONE - Retour immédiat
        // ============================================================
        log.info("[BOOK-UPLOAD] Launching async enrichment v2 for book: {}", bookId);

        // Le status reste PENDING, l'enrichissement se fait en background
        savedBook.setProcessingDetails("Upload terminé, enrichissement en cours...");
        bookRepository.save(savedBook);

        // Lancer l'enrichissement en arrière-plan (retourne immédiatement)
        asyncEnrichmentService.enrichBookAsync(bookId);

        log.info("[BOOK-UPLOAD] Upload complete (async enrichment started) - ID: {}, Status: {}, Title: {}",
                savedBook.getId(), savedBook.getStatus(), savedBook.getTitle());

        // Retourne 202 Accepted pour indiquer que le traitement est en cours
        return ResponseEntity.accepted().body(savedBook.getId().toString());
    }

    @GetMapping("/{bookId}/cover")
    public ResponseEntity<byte[]> getBookCover(@PathVariable UUID bookId) {
        log.debug("Attempting to get cover for book ID: {}", bookId);

        if (!bookRepository.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found.");
        }

        Optional<byte[]> coverBytes = bookCoverService.getCover(bookId);

        if (coverBytes.isPresent()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cover-" + bookId + ".png\"")
                    .body(coverBytes.get());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cover not available.");
    }

    /**
     * Delete a book and all associated data
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {
        
        String supabaseUserId = authenticationHelper.getSupabaseUserId(jwtPrincipal);
        log.info("Request to delete book {} by user {}", bookId, supabaseUserId);
        
        // Find the book
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isEmpty()) {
            log.warn("Attempted to delete non-existent book: {}", bookId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        
        Book book = bookOptional.get();
        
        // Get current user
        User currentUser = userService.findUserBySupabaseId(supabaseUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        boolean isOwner = book.getUser() != null && book.getUser().getSupabaseUserId().equals(supabaseUserId);
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        boolean isPublic = book.getVisibilityStatus() == BookVisibilityStatus.PUBLIC;
        
        // Authorization logic:
        // - Private books: only owner can delete
        // - Public books: only admin can delete
        if (!isPublic && !isOwner) {
            log.warn("User {} attempted to delete private book {} they don't own", supabaseUserId, bookId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own private books");
        }
        
        if (isPublic && !isAdmin) {
            log.warn("Non-admin user {} attempted to delete public book {}", supabaseUserId, bookId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can delete public books");
        }
        
        try {
            // Delete associated files
            deleteBookFiles(book);
            
            // Delete from database (cascading will handle related data)
            bookRepository.delete(book);
            
            log.info("Successfully deleted {} book {} ({}) by {} {}", 
                    isPublic ? "public" : "private", 
                    bookId, 
                    book.getTitle(), 
                    isAdmin ? "admin" : "owner", 
                    supabaseUserId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting book {}: {}", bookId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete book");
        }
    }
    
    /**
     * Retry enrichment for a failed or partially enriched book.
     * Only the owner or admin can retry.
     */
    @PostMapping("/{bookId}/retry-enrichment")
    public ResponseEntity<Map<String, String>> retryEnrichment(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Jwt jwtPrincipal) {

        User currentUser = authenticationHelper.getRequiredAuthenticatedUser(jwtPrincipal);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        // Check authorization: owner or admin
        boolean isOwner = book.getUser() != null && book.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only the book owner or admin can retry enrichment");
        }

        // Check status: only PARTIALLY_ENRICHED or FAILED can be retried
        if (book.getStatus() != BookStatus.PARTIALLY_ENRICHED && book.getStatus() != BookStatus.FAILED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Only books with status PARTIALLY_ENRICHED or FAILED can be retried. Current status: " + book.getStatus());
        }

        log.info("[RETRY-ENRICH] User {} retrying enrichment for book {} (status: {}, lastPage: {})",
            currentUser.getSupabaseUserId(), bookId, book.getStatus(), book.getLastSuccessfullyProcessedPageIndex());

        // Launch async enrichment (will resume from lastSuccessfullyProcessedPageIndex)
        asyncEnrichmentService.enrichBookAsync(bookId);

        Map<String, String> response = new HashMap<>();
        response.put("bookId", bookId.toString());
        response.put("message", "Enrichment retry started");
        response.put("resumeFromPage", String.valueOf(book.getLastSuccessfullyProcessedPageIndex() + 1));

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Delete all files associated with a book from R2
     */
    private void deleteBookFiles(Book book) {
        UUID bookId = book.getId();

        // Delete cover image from R2
        if (bookCoverService.coverExists(bookId)) {
            if (bookCoverService.deleteCover(bookId)) {
                log.info("Deleted cover image from R2 for book {}", bookId);
            } else {
                log.warn("Failed to delete cover image from R2 for book {}", bookId);
            }
        }

        // Delete raw PDF from R2
        String rawPdfKey = "books/raw/" + bookId + ".pdf";
        if (r2StorageService.deleteFile(rawPdfKey)) {
            log.info("Deleted raw PDF from R2: {}", rawPdfKey);
        }

        // Delete structured JSON from R2
        String structuredKey = "batch/structured/" + bookId + "-structured.json";
        if (r2StorageService.deleteFile(structuredKey)) {
            log.info("Deleted structured JSON from R2: {}", structuredKey);
        }
    }

    /**
     * Apply translated metadata to DTO if available.
     * Falls back to Arabic (original) if translation not found.
     */
    private void applyTranslation(BookSummaryDto dto, Book book, String lang) {
        if ("ar".equals(lang)) {
            // Arabic is the default, no translation needed
            return;
        }

        Optional<BookMetadataTranslation> translation =
                translationRepository.findByBookIdAndLanguageCode(book.getId(), lang);

        if (translation.isPresent()) {
            BookMetadataTranslation t = translation.get();
            if (t.getTitle() != null) {
                dto.setTitle(t.getTitle());
            }
            if (t.getGenre() != null) {
                dto.setGenre(t.getGenre());
            }
            if (t.getDescription() != null) {
                dto.setDescription(t.getDescription());
            }
            if (t.getAuthor() != null) {
                dto.setAuthorName(t.getAuthor());
            }
        }
        // If translation not found, keep Arabic values (already set)
    }

}