package com.backcover.repository;

import com.backcover.model.WordTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WordTranslationRepository extends JpaRepository<WordTranslation, UUID> {
    
    /**
     * Trouve une traduction existante pour éviter la duplication
     */
    Optional<WordTranslation> findByWordAnalysisIdAndLanguageCodeAndTranslationText(
            UUID wordAnalysisId, String languageCode, String translationText);
    
    /**
     * Trouve toutes les traductions pour une analyse de mot dans une langue donnée
     */
    List<WordTranslation> findByWordAnalysisIdAndLanguageCodeOrderByConfidenceScoreDesc(
            UUID wordAnalysisId, String languageCode);
    
    /**
     * Trouve toutes les traductions pour une analyse de mot
     */
    List<WordTranslation> findByWordAnalysisIdOrderByLanguageCodeAscConfidenceScoreDesc(
            UUID wordAnalysisId);
    
    /**
     * Trouve la meilleure traduction (plus haut score de confiance) pour une analyse et une langue
     */
    @Query("SELECT wt FROM WordTranslation wt WHERE wt.wordAnalysisId = :wordAnalysisId " +
           "AND wt.languageCode = :languageCode ORDER BY wt.confidenceScore DESC LIMIT 1")
    Optional<WordTranslation> findBestTranslation(@Param("wordAnalysisId") UUID wordAnalysisId, 
                                                 @Param("languageCode") String languageCode);
    
    /**
     * Compte le nombre de traductions distinctes pour une analyse de mot
     */
    long countByWordAnalysisIdAndLanguageCode(UUID wordAnalysisId, String languageCode);
    
    /**
     * Trouve les traductions par source
     */
    List<WordTranslation> findByWordAnalysisIdAndLanguageCodeAndSourceOrderByConfidenceScoreDesc(
            UUID wordAnalysisId, String languageCode, String source);
}