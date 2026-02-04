// Dans com/backcover/service/ia/DetailedWordAnalyzerService.java
package com.backcover.service.ia;

import com.backcover.dto.WordAnalysisDto; // <<< Nous allons définir ce DTO
import com.fasterxml.jackson.databind.JsonNode; // Alternative si on veut garder JsonNode pour l'instant

import java.io.IOException;
import java.util.UUID;

public interface DetailedWordAnalyzerService {

    /**
     * Fournit une analyse linguistique détaillée d'un mot spécifique, en tenant compte de son contexte.
     * L'implémentation peut utiliser des services d'IA, des bases de données lexicales, etc.
     *
     * @param wordTextInContext Le texte exact du mot tel qu'il apparaît (avec Tashkeel).
     * @param paragraphContext Le texte du paragraphe environnant le mot.
     * @param bookTitle Le titre du livre (pour contexte supplémentaire).
     * @param bookId L'ID du livre.
     * @param pageNumber L'index de la page (base 0).
     * @param wordInstanceId L'identifiant unique de l'instance du mot.
     * @param targetLanguageCode Le code de la langue de traduction cible (ex: 'fr', 'en', 'ar').
     * @return Un objet WordAnalysisDto contenant les détails de l'analyse.
     * @throws IOException Si une erreur de communication ou de traitement irrécupérable survient.
     * @throws IllegalArgumentException Si les paramètres d'entrée sont invalides.
     */
    WordAnalysisDto analyzeWord(String wordTextInContext,
                                String paragraphContext,
                                String bookTitle,
                                UUID bookId,
                                Integer pageNumber,
                                String wordInstanceId,
                                String targetLanguageCode) throws IOException, IllegalArgumentException;
}