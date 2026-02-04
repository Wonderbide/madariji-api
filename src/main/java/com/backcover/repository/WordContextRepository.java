package com.backcover.repository;

import com.backcover.model.WordContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WordContextRepository extends JpaRepository<WordContext, UUID> {
    
    /**
     * Trouve un contexte de mot spécifique (pour éviter les doublons)
     */
    Optional<WordContext> findByBookIdAndPageNumberAndWordInstanceId(
            UUID bookId, Integer pageNumber, String wordInstanceId);
    
    /**
     * Trouve tous les contextes pour un livre et une page
     */
    List<WordContext> findByBookIdAndPageNumberOrderByWordInstanceId(
            UUID bookId, Integer pageNumber);
    
    /**
     * Trouve tous les contextes utilisant une traduction spécifique
     */
    List<WordContext> findByWordTranslationId(UUID wordTranslationId);
    
    /**
     * Trouve tous les contextes pour une analyse de mot donnée
     */
    List<WordContext> findByWordAnalysisId(UUID wordAnalysisId);
    
    /**
     * Requête jointe pour récupérer le contexte avec traduction et analyse
     */
    @Query("SELECT wc FROM WordContext wc " +
           "LEFT JOIN FETCH wc.wordTranslation " +
           "LEFT JOIN FETCH wc.wordAnalysis " +
           "LEFT JOIN FETCH wc.paragraphContext " +
           "WHERE wc.bookId = :bookId AND wc.pageNumber = :pageNumber AND wc.wordInstanceId = :wordInstanceId")
    Optional<WordContext> findWithDetailsBy(@Param("bookId") UUID bookId, 
                                           @Param("pageNumber") Integer pageNumber, 
                                           @Param("wordInstanceId") String wordInstanceId);
    
    /**
     * Trouve tous les contextes d'un livre avec détails
     */
    @Query("SELECT wc FROM WordContext wc " +
           "LEFT JOIN FETCH wc.wordTranslation " +
           "LEFT JOIN FETCH wc.wordAnalysis " +
           "WHERE wc.bookId = :bookId ORDER BY wc.pageNumber, wc.wordInstanceId")
    List<WordContext> findByBookIdWithDetails(@Param("bookId") UUID bookId);
    
    /**
     * Compte les utilisations d'une traduction
     */
    long countByWordTranslationId(UUID wordTranslationId);
    
    /**
     * Vérifie si un contexte existe déjà
     */
    boolean existsByBookIdAndPageNumberAndWordInstanceId(
            UUID bookId, Integer pageNumber, String wordInstanceId);
}