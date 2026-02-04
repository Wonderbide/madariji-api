package com.backcover.repository;

import com.backcover.model.user.UserWordList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWordListRepository extends JpaRepository<UserWordList, UUID> {
    
    /**
     * Find the default word list for a user by their internal user ID
     * @deprecated Use findByUserIdAndLanguageCodeAndIsDefaultTrue instead
     */
    @Deprecated
    Optional<UserWordList> findByUserIdAndIsDefaultTrue(UUID userId);
    
    /**
     * Find the default word list for a user by their email
     * @deprecated Use findByUserEmailAndLanguageCodeAndIsDefaultTrue instead
     */
    @Deprecated
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.isDefault = true")
    Optional<UserWordList> findByUserSupabaseUserIdAndIsDefaultTrue(@Param("email") String email);
    
    /**
     * Check if a default word list exists for a user
     * @deprecated Use existsByUserIdAndLanguageCodeAndIsDefaultTrue instead
     */
    @Deprecated
    boolean existsByUserIdAndIsDefaultTrue(UUID userId);
    
    /**
     * Find all word lists for a user by their internal user ID
     */
    List<UserWordList> findByUserId(UUID userId);
    
    // === NEW METHODS WITH LANGUAGE SUPPORT ===
    
    /**
     * Find the default word list for a user and language by their internal user ID
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.id = :userId AND uwl.languageCode = :languageCode AND uwl.isDefault = true")
    Optional<UserWordList> findByUserIdAndLanguageCodeAndIsDefaultTrue(@Param("userId") UUID userId, @Param("languageCode") String languageCode);
    
    /**
     * Find the default word list for a user and language by their email
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.languageCode = :languageCode AND uwl.isDefault = true")
    Optional<UserWordList> findByUserEmailAndLanguageCodeAndIsDefaultTrue(@Param("email") String email, @Param("languageCode") String languageCode);
    
    /**
     * @deprecated Use findByUserEmailAndLanguageCodeAndIsDefaultTrue instead
     */
    @Deprecated
    default Optional<UserWordList> findByUserSupabaseUserIdAndLanguageCodeAndIsDefaultTrue(String supabaseUserId, String languageCode) {
        // Since supabaseUserId now returns email, we can use it directly
        return findByUserEmailAndLanguageCodeAndIsDefaultTrue(supabaseUserId, languageCode);
    }
    
    /**
     * Check if a default word list exists for a user and language
     */
    @Query("SELECT COUNT(uwl) > 0 FROM UserWordList uwl WHERE uwl.user.id = :userId AND uwl.languageCode = :languageCode AND uwl.isDefault = true")
    boolean existsByUserIdAndLanguageCodeAndIsDefaultTrue(@Param("userId") UUID userId, @Param("languageCode") String languageCode);
    
    /**
     * Find all word lists for a user and language
     */
    List<UserWordList> findByUserIdAndLanguageCode(UUID userId, String languageCode);
    
    /**
     * Find all word lists for a user by email and language  
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.languageCode = :languageCode")
    List<UserWordList> findByUserEmailAndLanguageCode(@Param("email") String email, @Param("languageCode") String languageCode);
    
    /**
     * @deprecated Use findByUserEmailAndLanguageCode instead
     */
    @Deprecated
    default List<UserWordList> findByUserSupabaseUserIdAndLanguageCode(String supabaseUserId, String languageCode) {
        return findByUserEmailAndLanguageCode(supabaseUserId, languageCode);
    }
    
    // === BOOK-SPECIFIC METHODS ===
    
    /**
     * Find book-specific word list for a user by email, language, and book ID
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.languageCode = :languageCode AND uwl.book.id = :bookId")
    Optional<UserWordList> findByUserEmailAndLanguageCodeAndBookId(@Param("email") String email, @Param("languageCode") String languageCode, @Param("bookId") UUID bookId);
    
    /**
     * @deprecated Use findByUserEmailAndLanguageCodeAndBookId instead
     */
    @Deprecated
    default Optional<UserWordList> findByUserSupabaseUserIdAndLanguageCodeAndBookId(String supabaseUserId, String languageCode, UUID bookId) {
        return findByUserEmailAndLanguageCodeAndBookId(supabaseUserId, languageCode, bookId);
    }
    
    /**
     * Find all book-specific word lists for a user by email and book ID (all languages)
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.book.id = :bookId")
    List<UserWordList> findByUserEmailAndBookId(@Param("email") String email, @Param("bookId") UUID bookId);
    
    /**
     * @deprecated Use findByUserEmailAndBookId instead
     */
    @Deprecated
    default List<UserWordList> findByUserSupabaseUserIdAndBookId(String supabaseUserId, UUID bookId) {
        return findByUserEmailAndBookId(supabaseUserId, bookId);
    }
    
    /**
     * Check if a book-specific word list exists for a user and language
     */
    @Query("SELECT COUNT(uwl) > 0 FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.languageCode = :languageCode AND uwl.book.id = :bookId")
    boolean existsByUserEmailAndLanguageCodeAndBookId(@Param("email") String email, @Param("languageCode") String languageCode, @Param("bookId") UUID bookId);
    
    /**
     * @deprecated Use existsByUserEmailAndLanguageCodeAndBookId instead
     */
    @Deprecated
    default boolean existsByUserSupabaseUserIdAndLanguageCodeAndBookId(String supabaseUserId, String languageCode, UUID bookId) {
        return existsByUserEmailAndLanguageCodeAndBookId(supabaseUserId, languageCode, bookId);
    }
    
    /**
     * Find all language-based (non-book-specific) word lists for a user by email and language
     * These are the traditional lists where book_id is NULL
     */
    @Query("SELECT uwl FROM UserWordList uwl WHERE uwl.user.email = :email AND uwl.languageCode = :languageCode AND uwl.book IS NULL")
    List<UserWordList> findByUserEmailAndLanguageCodeAndBookIsNull(@Param("email") String email, @Param("languageCode") String languageCode);
    
    /**
     * @deprecated Use findByUserEmailAndLanguageCodeAndBookIsNull instead
     */
    @Deprecated
    default List<UserWordList> findByUserSupabaseUserIdAndLanguageCodeAndBookIsNull(String supabaseUserId, String languageCode) {
        return findByUserEmailAndLanguageCodeAndBookIsNull(supabaseUserId, languageCode);
    }
    
    /**
     * Delete all word lists by user ID (for test cleanup)
     */
    void deleteByUserId(UUID userId);
}