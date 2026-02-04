package com.backcover.repository;

import com.backcover.model.user.UserWordListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWordListItemRepository extends JpaRepository<UserWordListItem, UUID> {
    
    /**
     * Find items from a word list, ordered by addition date (newest first)
     */
    Page<UserWordListItem> findByWordListIdOrderByAddedAtDesc(UUID wordListId, Pageable pageable);
    
    /**
     * Find all items from a word list, ordered by addition date (newest first)
     */
    List<UserWordListItem> findByWordListIdOrderByAddedAtDesc(UUID wordListId);
    
    /**
     * Find all items from a word list, ordered by page number then by addition time
     */
    List<UserWordListItem> findByWordListIdOrderByPageNumberAscAddedAtAsc(UUID wordListId);
    
    /**
     * Find all items from a word list, ordered by page number then by addition time (newest first)
     */
    List<UserWordListItem> findByWordListIdOrderByPageNumberAscAddedAtDesc(UUID wordListId);
    
    /**
     * Find a specific word item by its list ID, book ID, and word instance ID
     */
    Optional<UserWordListItem> findByWordListIdAndBookIdAndWordInstanceId(UUID wordListId, UUID bookId, String wordInstanceId);
    
    /**
     * Delete a word list item by ID, ensuring it belongs to the specified user (by email)
     * @return number of deleted rows
     */
    @Modifying
    @Query("DELETE FROM UserWordListItem i WHERE i.id = :itemId AND i.wordList.user.email = :email")
    int deleteByIdAndWordListUserEmail(@Param("itemId") UUID itemId, @Param("email") String email);
    
    /**
     * @deprecated Use deleteByIdAndWordListUserEmail instead
     */
    @Deprecated
    default int deleteByIdAndWordListUserSupabaseUserId(UUID itemId, String supabaseUserId) {
        return deleteByIdAndWordListUserEmail(itemId, supabaseUserId);
    }
    
    /**
     * OPTIMIZED: Delete a word list item by ID, list ID, and user email (most efficient)
     * @return number of deleted rows
     */
    @Modifying
    @Query("DELETE FROM UserWordListItem i WHERE i.id = :itemId AND i.wordList.id = :listId AND i.wordList.user.email = :email")
    int deleteByIdAndWordListIdAndWordListUserEmail(@Param("itemId") UUID itemId, @Param("listId") UUID listId, @Param("email") String email);
    
    /**
     * @deprecated Use deleteByIdAndWordListIdAndWordListUserEmail instead
     */
    @Deprecated
    default int deleteByIdAndWordListIdAndWordListUserSupabaseUserId(UUID itemId, UUID listId, String supabaseUserId) {
        return deleteByIdAndWordListIdAndWordListUserEmail(itemId, listId, supabaseUserId);
    }
    
    /**
     * OPTIMIZED: Simple delete by ID only (security check done separately)
     * @return number of deleted rows
     */
    @Modifying
    @Query("DELETE FROM UserWordListItem i WHERE i.id = :itemId")
    int deleteByItemId(@Param("itemId") UUID itemId);
    
    /**
     * OPTIMIZED: Find item with user verification in one query
     * @return the item if it belongs to user, empty otherwise
     */
    @Query("SELECT i FROM UserWordListItem i WHERE i.id = :itemId AND i.wordList.user.email = :email")
    Optional<UserWordListItem> findByIdAndUserEmail(@Param("itemId") UUID itemId, @Param("email") String email);
    
    /**
     * @deprecated Use findByIdAndUserEmail instead
     */
    @Deprecated
    default Optional<UserWordListItem> findByIdAndUserSupabaseId(UUID itemId, String supabaseUserId) {
        return findByIdAndUserEmail(itemId, supabaseUserId);
    }
    
    /**
     * Check if an item exists in the specified list for the given book and word instance
     */
    boolean existsByWordListIdAndBookIdAndWordInstanceId(UUID wordListId, UUID bookId, String wordInstanceId);
    
    /**
     * Count items in a word list
     */
    long countByWordListId(UUID wordListId);
    
    /**
     * Find all items for a specific book and user, ordered by page then chronologically desc
     */
    @Query("SELECT i FROM UserWordListItem i WHERE i.user.email = :email AND i.book.id = :bookId ORDER BY i.pageNumber ASC, i.addedAt DESC")
    List<UserWordListItem> findByUserEmailAndBookIdOrderByPageNumberAscAddedAtDesc(@Param("email") String email, @Param("bookId") UUID bookId);
    
    /**
     * @deprecated Use findByUserEmailAndBookIdOrderByPageNumberAscAddedAtDesc instead
     */
    @Deprecated
    default List<UserWordListItem> findByUserSupabaseUserIdAndBookIdOrderByPageNumberAscAddedAtDesc(String supabaseUserId, UUID bookId) {
        return findByUserEmailAndBookIdOrderByPageNumberAscAddedAtDesc(supabaseUserId, bookId);
    }
    
    /**
     * Find all items for a specific book, user and list language, ordered by page then chronologically desc
     */
    @Query("SELECT i FROM UserWordListItem i WHERE i.user.email = :email AND i.book.id = :bookId AND i.wordList.languageCode = :languageCode ORDER BY i.pageNumber ASC, i.addedAt DESC")
    List<UserWordListItem> findByUserEmailAndBookIdAndListLanguageOrderByPageNumberAscAddedAtDesc(@Param("email") String email, @Param("bookId") UUID bookId, @Param("languageCode") String languageCode);
    
    /**
     * @deprecated Use findByUserEmailAndBookIdAndListLanguageOrderByPageNumberAscAddedAtDesc instead
     */
    @Deprecated
    default List<UserWordListItem> findByUserSupabaseUserIdAndBookIdAndListLanguageOrderByPageNumberAscAddedAtDesc(String supabaseUserId, UUID bookId, String languageCode) {
        return findByUserEmailAndBookIdAndListLanguageOrderByPageNumberAscAddedAtDesc(supabaseUserId, bookId, languageCode);
    }
    
    /**
     * Delete all items by word list ID (for test cleanup)
     */
    void deleteByWordListId(UUID wordListId);
}