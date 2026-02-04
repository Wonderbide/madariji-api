package com.backcover.repository;

import com.backcover.model.ContextualWordMeaning; // Importer l'entité
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité ContextualWordMeaning, gérant les sens et traductions
 * spécifiques des mots dans leur contexte (livre, page, instance).
 */
@Repository
public interface ContextualWordMeaningRepository extends JpaRepository<ContextualWordMeaning, UUID> { // Gère ContextualWordMeaning avec clé UUID

    /**
     * Recherche un sens contextuel spécifique basé sur la clé composite :
     * livre, page, identifiant d'instance du mot, et langue de traduction.
     * C'est la méthode principale pour vérifier le cache DB avant d'appeler le LLM.
     *
     * @param bookId                 L'ID du livre.
     * @param pageNumber             L'index de la page.
     * @param wordInstanceId         L'identifiant unique de l'instance du mot sur la page.
     * @param translationLanguageCode La langue cible de la traduction recherchée (ex: "fr").
     * @return Un Optional contenant l'entité ContextualWordMeaning si trouvée.
     */
    Optional<ContextualWordMeaning> findByBookIdAndPageNumberAndWordInstanceIdAndTranslationLanguageCode(
            UUID bookId,
            Integer pageNumber,
            String wordInstanceId,
            String translationLanguageCode
    );

    // --- Autres méthodes utiles (optionnelles) ---

    /**
     * Trouve tous les sens contextuels enregistrés pour un livre spécifique.
     * (Peut être utile pour l'export, l'analyse, ou l'administration).
     *
     * @param bookId L'ID du livre.
     * @return Une liste des sens contextuels pour ce livre.
     */
    List<ContextualWordMeaning> findByBookId(UUID bookId);

    /**
     * Trouve tous les sens contextuels associés à une analyse structurelle spécifique.
     * (Peut être utile pour voir où et comment une analyse particulière est utilisée).
     *
     * @param wordAnalysisId L'ID de l'entité WordAnalysis.
     * @return Une liste des sens contextuels liés à cette analyse.
     */
    List<ContextualWordMeaning> findByWordAnalysisId(UUID wordAnalysisId);

    /**
     * Trouve tous les sens contextuels pour un livre et une page donnés.
     *
     * @param bookId L'ID du livre.
     * @param pageNumber L'index de la page.
     * @return Une liste des sens contextuels pour cette page spécifique.
     */
    List<ContextualWordMeaning> findByBookIdAndPageNumber(UUID bookId, Integer pageNumber);

}