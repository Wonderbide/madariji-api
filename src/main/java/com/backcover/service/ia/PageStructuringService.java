// Dans com/backcover/service/ia/PageStructuringService.java
package com.backcover.service.ia;

import com.backcover.dto.TextBlockDto; // Assurez-vous que le chemin est correct
import java.util.List;

public interface PageStructuringService {

    /**
     * Analyse un texte de page brut (généralement issu d'un OCR),
     * le structure en blocs sémantiques, applique la vocalisation (Tashkeel),
     * et détermine si la page contient du contenu significatif.
     *
     * @param rawPageText Le texte brut de la page à analyser.
     * @return Un PageStructureResult contenant l'indication de conservation de la page
     *         et la liste des blocs de texte structurés et vocalisés.
     *         Retourne un résultat indiquant de ne pas garder la page (keepPage=false)
     *         avec une liste de blocs vide en cas d'échec de l'analyse ou si le texte est vide.
     */
    PageStructureResult structurePageText(String rawPageText);

    /**
     * Overloaded method with tracking support for AI usage monitoring.
     * Default implementation delegates to the basic method.
     *
     * @param rawPageText Le texte brut de la page à analyser.
     * @param userId ID de l'utilisateur pour le tracking (peut être null)
     * @param bookId ID du livre pour le tracking (peut être null)
     * @return Un PageStructureResult avec tracking des coûts et tokens.
     */
    default PageStructureResult structurePageText(String rawPageText, java.util.UUID userId, java.util.UUID bookId) {
        return structurePageText(rawPageText);
    }

    // Le DTO de résultat (peut être un record ou une classe)
    // Ce nom est plus générique que "GeminiOcrAnalysisResult"
    record PageStructureResult(
            boolean keepPage,
            List<TextBlockDto> blocks
    ) {}
}