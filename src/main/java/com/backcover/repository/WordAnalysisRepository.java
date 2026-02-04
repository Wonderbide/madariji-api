package com.backcover.repository;

import com.backcover.model.WordAnalysis; // L'entité représentant la table word_analysis
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Pour des requêtes plus spécifiques si besoin
import org.springframework.data.repository.query.Param; // Pour les paramètres nommés dans @Query
import org.springframework.stereotype.Repository;

// Importer List si ce n'est pas déjà fait
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité WordAnalysis, qui stocke les données structurelles
 * d'une analyse spécifique (type, racine, détails) pour un DictionaryWord.
 */
@Repository
public interface WordAnalysisRepository extends JpaRepository<WordAnalysis, UUID> { // Gère l'entité WordAnalysis avec une clé primaire UUID

    // --- Méthodes pour la nouvelle logique ---

    /**
     * Trouve toutes les analyses existantes associées à un mot spécifique du dictionnaire.
     * Utilisé pour vérifier si une analyse structurelle identique existe déjà avant d'en créer une nouvelle.
     *
     * @param dictionaryWordId L'ID du DictionaryWord (table dictionary_word).
     * @return Une liste de toutes les WordAnalysis pour ce DictionaryWord. Peut être vide.
     */
    List<WordAnalysis> findByDictionaryWordId(UUID dictionaryWordId);

    /**
     * Recherche une WordAnalysis spécifique basée sur l'ID du mot du dictionnaire ET
     * le contenu exact des données d'analyse (stocké comme JSONB/String).
     * NOTE: Cette méthode est un exemple et peut nécessiter une implémentation personnalisée
     * ou une approche différente (ex: hash du JSON) pour être performante,
     * car la comparaison directe de champs JSONB en JPQL/SQL peut être complexe ou inefficace
     * selon la base de données. L'approche actuelle est de récupérer par dictionaryWordId
     * et de filtrer en Java (voir WordAnalysisService).
     *
     * @param dictionaryWordId L'ID du DictionaryWord.
     * @param analysisDataJsonString La représentation String du JSON analysis_data à rechercher.
     * @return Un Optional contenant la WordAnalysis correspondante si trouvée.
     */
    // Exemple avec @Query (peut nécessiter des ajustements selon la BDD et la version d'Hibernate)
    // Attention : la comparaison ::text peut être coûteuse.
    /*
    @Query("SELECT wa FROM WordAnalysis wa WHERE wa.dictionaryWordId = :dictId AND function('jsonb_text', wa.analysisData) = :dataJson")
    Optional<WordAnalysis> findByDictionaryWordIdAndAnalysisDataJson(
        @Param("dictId") UUID dictionaryWordId,
        @Param("dataJson") String analysisDataJsonString
    );
    */
    // Pour l'instant, on se fie à la recherche par dictionaryWordId et au filtrage Java dans le service.


    // --- Méthodes précédentes (peuvent être supprimées si plus utilisées) ---

    /**
     * (Obsolète avec le nouveau modèle ?) Trouve la première analyse basée sur le mot textuel,
     * l'ID du livre et le numéro de page.
     * Cette méthode liait l'analyse directement au contexte du livre/page, ce qui est maintenant
     * géré par ContextualWordMeaning. Garder seulement si un ancien usage persiste.
     *
     * @param word Le texte du mot.
     * @param bookId L'ID du livre.
     * @param pageNumber L'index de la page.
     * @return Un Optional contenant la WordAnalysis si trouvée selon ces anciens critères.
     */
    // Optional<WordAnalysis> findFirstByWordAndBookIdAndPageNumber(String word, UUID bookId, Integer pageNumber);

    /**
     * (Obsolète avec le nouveau modèle ?) Trouve la première analyse pour un mot textuel
     * dans un livre donné, sans tenir compte de la page.
     *
     * @param word Le texte du mot.
     * @param bookId L'ID du livre.
     * @return Un Optional contenant la WordAnalysis si trouvée.
     */
    // Optional<WordAnalysis> findFirstByWordAndBookId(String word, UUID bookId);

    // --- Methods for Quiz System ---
    
    /**
     * Find words by book IDs excluding already studied words
     * Uses ContextualWordMeaning to link WordAnalysis to books
     */
    @Query("SELECT DISTINCT cwm.wordAnalysis FROM ContextualWordMeaning cwm " +
           "WHERE cwm.bookId IN :bookIds AND cwm.wordAnalysis.id NOT IN :excludedIds")
    List<WordAnalysis> findByBookIdInAndIdNotIn(@Param("bookIds") List<UUID> bookIds, @Param("excludedIds") List<UUID> excludedIds);
    
    /**
     * Find words excluding already studied words
     */
    @Query("SELECT wa FROM WordAnalysis wa WHERE wa.id NOT IN :excludedIds")
    List<WordAnalysis> findByIdNotIn(@Param("excludedIds") List<UUID> excludedIds);
}