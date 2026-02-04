package com.backcover.config;

import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes essential prompts on startup if they don't exist.
 * Required for fresh database deployments.
 */
@Configuration
@Profile("!test")
public class PromptDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PromptDataInitializer.class);

    private static final String WORD_ANALYSIS_PROMPT = """
        **Tâche :** Analyse grammaticalement et sémantiquement le mot arabe cible fourni, en utilisant **impérativement le contexte de paragraphe fourni** pour déterminer le sens et la traduction les plus précis dans la langue demandée (%s).

        **Contexte Fourni :**
        *   **Mot Cible :** %s
        *   **Titre du Livre :** %s
        *   **Genre du Livre :** %s
        *   **Auteur :** %s
        *   **Description du Livre :** %s
        *   **Contexte de Paragraphe (extrait de la page actuelle) :**
        --- DEBUT CONTEXTE ---
        %s
        --- FIN CONTEXTE ---
        *   **Langue de traduction demandée :** %s

        **Instructions Générales :**
        1.  Concentre ton analyse **exclusivement sur le "Mot Cible"** fourni ci-dessus (`%s`).
        2.  **CRITIQUE** : Lis attentivement le "Contexte de Paragraphe" pour comprendre si le mot fait partie d'une expression idiomatique. Cela influence UNIQUEMENT le champ "meaning", PAS le champ "translation".
        3.  **IMPORTANT** : Utilise les métadonnées du livre (genre, auteur, description) pour orienter ton analyse vers le domaine thématique approprié.
        4.  Pour les termes religieux et théologiques, privilégie systématiquement les définitions et interprétations conformes aux sources premières (Coran, Sunna) telles que comprises par les premières générations (salaf) et les savants du hadith.
        5.  Détermine le type grammatical principal du "Mot Cible" ("اسم", "فعل", "حرف", ou "unknown").
            **RÈGLE IMPORTANTE POUR LES HARFS**: Si le mot est annexé à un harf (comme بـ, لـ, كـ, فـ, وـ), le mot principal doit être considéré comme un "اسم" (ism) et non comme un "حرف" (harf).

        **CHAMPS LINGUISTIQUES OBLIGATOIRES :**

        6.  **jidar** (الجذر المجرد) : La forme nue du mot, dévêtue de toutes les particules annexées.
        7.  **root** (الجذر الثلاثي) : La racine trilitère ou quadrilitère avec espaces entre les lettres.
        8.  **masdar** (المصدر) : Le nom d'action associé au mot.
        9.  **wazn** (الوزن) : Le schème/pattern morphologique.
        10.  Remplis l'objet `details` selon le type grammatical du "Mot Cible".
        11. **TRADUCTION DYNAMIQUE** : Fournis la traduction du "Mot Cible" dans la langue spécifiée (%s).
        12. **DISTINCTION ABSOLUE entre "translation" et "meaning"**.
        13. **Identifie les champs lexicaux** : Fournis de 1 à 3 champs lexicaux dans `"lexical_fields"`.

        **FORMAT DE SORTIE JSON STRICT :**
        1.  Ta réponse doit être **UNIQUEMENT** un objet JSON valide. Pas de texte avant/après. Pas de ```json.
        2.  Structure JSON attendue :
            {
              "word": "VALEUR_EXACTE_DU_MOT_CIBLE_FOURNI",
              "type": "TYPE_TROUVÉ",
              "translation": "TRADUCTION_DANS_LA_LANGUE_DEMANDÉE",
              "jidar": "FORME_NUE_VOCALISÉE",
              "root": "ج ذ ر",
              "masdar": "المصدر" | null,
              "wazn": "الوزن" | null,
              "details": { /* OBJET DÉTAILS SELON TYPE */ } | null,
              "lexical_fields": ["champ1", "champ2"]
            }
        3.  **RÈGLE ABSOLUMENT CRITIQUE : La valeur de la clé `"word"` DOIT ÊTRE EXACTEMENT LA MÊME que le "Mot Cible" fourni (`%s`).**

        **Exécute maintenant l'analyse pour le "Mot Cible" : %s**
        """;

    private static final String PAGE_STRUCTURING_PROMPT = """
        Advanced document structuring: Analyze the OCR text and create a precise page-by-page structure.
        Input: %s

        Requirements:
        - Identify exact page boundaries
        - Preserve all content including headers/footers
        - Maintain paragraph structure
        - Detect page numbers

        Output: JSON with pages array containing page_number, content, paragraphs.
        """;

    @Bean
    @Transactional
    CommandLineRunner initializePrompts(PromptTemplateRepository promptRepository) {
        return args -> {
            log.info("=== PromptDataInitializer: Checking essential prompts ===");

            // Initialize WORD_ANALYSIS_V1 if not exists
            if (!promptRepository.existsByPromptKeyAndIsActiveTrue("WORD_ANALYSIS_V1")) {
                log.info("Creating WORD_ANALYSIS_V1 prompt...");
                PromptTemplate wordAnalysis = new PromptTemplate();
                wordAnalysis.setPromptKey("WORD_ANALYSIS_V1");
                wordAnalysis.setPromptText(WORD_ANALYSIS_PROMPT);
                wordAnalysis.setVersion("5.0");
                wordAnalysis.setIsActive(true);
                promptRepository.save(wordAnalysis);
                log.info("WORD_ANALYSIS_V1 prompt created successfully");
            } else {
                log.info("WORD_ANALYSIS_V1 prompt already exists");
            }

            // Initialize PAGE_STRUCTURING_V5 if not exists
            if (!promptRepository.existsByPromptKeyAndIsActiveTrue("PAGE_STRUCTURING_V5")) {
                log.info("Creating PAGE_STRUCTURING_V5 prompt...");
                PromptTemplate pageStructuring = new PromptTemplate();
                pageStructuring.setPromptKey("PAGE_STRUCTURING_V5");
                pageStructuring.setPromptText(PAGE_STRUCTURING_PROMPT);
                pageStructuring.setVersion("5.0");
                pageStructuring.setIsActive(true);
                promptRepository.save(pageStructuring);
                log.info("PAGE_STRUCTURING_V5 prompt created successfully");
            } else {
                log.info("PAGE_STRUCTURING_V5 prompt already exists");
            }

            log.info("=== PromptDataInitializer: Complete ===");
        };
    }
}
