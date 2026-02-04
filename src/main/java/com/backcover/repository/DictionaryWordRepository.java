package com.backcover.repository;

import com.backcover.model.DictionaryWord; // Importer l'entité DictionaryWord
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité DictionaryWord, qui stocke les mots uniques
 * par langue.
 */
@Repository
public interface DictionaryWordRepository extends JpaRepository<DictionaryWord, UUID> { // Gère DictionaryWord avec clé UUID

    /**
     * Recherche un mot spécifique dans le dictionnaire par son texte exact
     * et son code de langue.
     * C'est la méthode principale pour vérifier si un mot existe déjà
     * avant de potentiellement l'insérer. La recherche doit être sensible à la casse
     * si nécessaire, ou une normalisation doit être appliquée avant l'appel.
     *
     * @param wordText     Le texte exact du mot à rechercher (ex: "كتاب").
     * @param languageCode Le code ISO de la langue (ex: "ar").
     * @return Un Optional contenant l'entité DictionaryWord si elle est trouvée.
     */
    Optional<DictionaryWord> findByWordTextAndLanguageCode(String wordText, String languageCode);

    // --- Autres méthodes potentielles (optionnelles pour l'instant) ---

    /**
     * Recherche tous les mots pour une langue donnée (pourrait être utile pour l'admin ou stats).
     *
     * @param languageCode Le code ISO de la langue.
     * @return Une liste de tous les DictionaryWord pour cette langue.
     */
    // List<DictionaryWord> findByLanguageCode(String languageCode);

    /**
     * Recherche un mot par son texte sans tenir compte de la langue (moins utile généralement).
     *
     * @param wordText Le texte du mot.
     * @return Une liste des DictionaryWord correspondant à ce texte dans différentes langues.
     */
    // List<DictionaryWord> findByWordText(String wordText);

}