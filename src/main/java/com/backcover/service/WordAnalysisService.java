// Dans com/backcover/service/WordAnalysisService.java
package com.backcover.service; // Ou com.backcover.service.ia.impl

import com.backcover.config.LanguageConfig;
import com.backcover.dto.WordAnalysisDto; // <<< IMPORTER LE NOUVEAU DTO
import com.backcover.model.*;
import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.*;
import com.backcover.repository.PromptTemplateRepository;
import com.backcover.service.ia.DetailedWordAnalyzerService; // <<< IMPORTER L'INTERFACE
import com.backcover.service.prompt.PromptUsageTracker;
import com.backcover.model.WordTranslation;
import com.backcover.service.AIModelConfigurationService;
import com.backcover.model.AIFlowConfiguration.FlowType;
import com.backcover.exception.ConfigurationNotFoundException;
import com.backcover.exception.WordAnalysisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
// ... autres imports ...
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier; // Pour le futur
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
// ...

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service("geminiDetailedWordAnalyzer") // Nom qualifié pour cette implémentation
public class WordAnalysisService implements DetailedWordAnalyzerService { // <<< IMPLÉMENTER L'INTERFACE

    private static final Logger log = LoggerFactory.getLogger(WordAnalysisService.class);

    @Value("${gemini.api.key}") private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;
    private final DictionaryWordRepository dictionaryWordRepository;
    private final WordAnalysisRepository wordAnalysisRepository;
    private final ContextualWordMeaningRepository contextualWordMeaningRepository;
    private final TranslationContextService translationContextService;
    private final PromptTemplateService promptTemplateService;
    private final LanguageConfig languageConfig;
    private final LexicalFieldTranslationService lexicalFieldTranslationService;
    private final PromptUsageTracker promptUsageTracker;
    private final PromptTemplateRepository promptTemplateRepository;
    private final AIModelConfigurationService aiModelConfigService;

    public WordAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper,
                               BookRepository bookRepository,
                               DictionaryWordRepository dictionaryWordRepository,
                               WordAnalysisRepository wordAnalysisRepository,
                               ContextualWordMeaningRepository contextualWordMeaningRepository,
                               TranslationContextService translationContextService,
                               PromptTemplateService promptTemplateService,
                               LanguageConfig languageConfig,
                               LexicalFieldTranslationService lexicalFieldTranslationService,
                               PromptUsageTracker promptUsageTracker,
                               PromptTemplateRepository promptTemplateRepository,
                               AIModelConfigurationService aiModelConfigService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.bookRepository = bookRepository;
        this.dictionaryWordRepository = dictionaryWordRepository;
        this.wordAnalysisRepository = wordAnalysisRepository;
        this.contextualWordMeaningRepository = contextualWordMeaningRepository;
        this.translationContextService = translationContextService;
        this.promptTemplateService = promptTemplateService;
        this.languageConfig = languageConfig;
        this.lexicalFieldTranslationService = lexicalFieldTranslationService;
        this.promptUsageTracker = promptUsageTracker;
        this.promptTemplateRepository = promptTemplateRepository;
        this.aiModelConfigService = aiModelConfigService;
    }

    @Override
    @Transactional
    public WordAnalysisDto analyzeWord(String wordTextInContext, String paragraphText, String bookTitle,
                                       UUID bookId, Integer pageNumber, String wordInstanceId, String targetLanguageCode)
            throws IOException, IllegalArgumentException {

        if (bookId == null) {
            throw new IllegalArgumentException("Contexte invalide fourni.");
        }
        
        // Validation et normalisation du targetLanguageCode
        if (targetLanguageCode == null || targetLanguageCode.isBlank()) {
            targetLanguageCode = "fr"; // Langue par défaut
        }
        final String targetTranslationLanguage = targetLanguageCode;
        log.info("Analyse demandée pour l'instance '{}' ('{}') avec langue cible '{}'...", wordInstanceId, wordTextInContext, targetTranslationLanguage);

        // --- 1. Recherche dans le cache DB (nouvelle architecture) ---
        Optional<WordContext> cachedContextOpt = translationContextService
                .findWordContextWithDetails(bookId, pageNumber, wordInstanceId);
        
        // Variable pour stocker l'analyse existante si on en trouve une
        com.backcover.model.WordAnalysis existingAnalysis = null;

        if (cachedContextOpt.isPresent()) {
            WordContext cachedContext = cachedContextOpt.get();
            UUID wordAnalysisId = cachedContext.getWordAnalysisId();
            
            // Récupérer l'analyse complète
            Optional<com.backcover.model.WordAnalysis> analysisEntityOpt = 
                    wordAnalysisRepository.findById(wordAnalysisId);
            
            if (analysisEntityOpt.isPresent()) {
                com.backcover.model.WordAnalysis analysisEntity = analysisEntityOpt.get();
                existingAnalysis = analysisEntity; // Stocker pour réutilisation potentielle
                
                // Récupérer la meilleure traduction pour cette analyse
                Optional<WordTranslation> bestTranslationOpt = translationContextService
                        .findBestTranslation(wordAnalysisId, targetTranslationLanguage);
                
                if (bestTranslationOpt.isPresent() && analysisEntity.getAnalysisData() != null) {
                    // Vérifier si on a les details dans la langue demandée
                    try {
                        JsonNode analysisData = objectMapper.readTree(analysisEntity.getAnalysisData());
                        boolean hasDetailsInTargetLanguage = false;
                        
                        if (analysisData.has("details_by_language")) {
                            JsonNode detailsByLang = analysisData.get("details_by_language");
                            hasDetailsInTargetLanguage = detailsByLang.has(targetTranslationLanguage);
                        } else if (analysisData.has("details") && "fr".equals(targetTranslationLanguage)) {
                            // Ancien format, supposé être en français
                            hasDetailsInTargetLanguage = true;
                        }
                        
                        if (hasDetailsInTargetLanguage) {
                            log.info("Cache HIT pour l'instance '{}' avec details dans la langue '{}'. Retour des données cachées.", 
                                    wordInstanceId, targetTranslationLanguage);
                            // Convertir le JSON stocké et la traduction en WordAnalysisDto
                            return convertToWordAnalysisDto(
                                    analysisData,                                            // JSON de l'analyse
                                    bestTranslationOpt.get().getTranslationText(),          // Texte de la traduction
                                    wordTextInContext,                                       // Mot original avec Tashkeel
                                    analysisEntity,                                          // Entité pour récupérer l'ID
                                    targetTranslationLanguage                                // Langue cible
                            );
                        } else {
                            log.info("Cache HIT pour l'instance '{}' mais details manquants pour la langue '{}'. Appel à l'IA pour obtenir les details.", 
                                    wordInstanceId, targetTranslationLanguage);
                            // Continuer vers l'appel IA pour obtenir les details dans la bonne langue
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Échec du parsing des données d'analyse cachées pour l'instance {}: {}", wordInstanceId, e.getMessage());
                        // Continuer pour appeler l'API si le parsing du cache échoue
                    }
                } else {
                    log.warn("Cache HIT pour l'instance '{}' mais traduction ou données d'analyse manquantes. Appel à l'IA.", wordInstanceId);
                }
            } else {
                log.warn("Cache HIT pour l'instance '{}' mais WordAnalysis non trouvé (ID: {}). Appel à l'IA.", wordInstanceId, wordAnalysisId);
            }
        } else {
            log.info("Cache MISS pour l'instance '{}'. Appel à l'IA.", wordInstanceId);
        }

        // --- 2. Cache Miss ou parsing cache échoué -> Appel LLM ---
        // Get userId and book metadata
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
        UUID userId = book.getUser() != null ? book.getUser().getId() : null;
        String bookGenre = book.getGenre();
        String bookDescription = book.getDescription();
        String bookAuthor = book.getAuthorName();

        JsonNode llmResponseJson; // Le JSON complet retourné par l'IA (contenant word, type, translation, root, details, canonical_form)
        try {
            llmResponseJson = callGeminiForAnalysis(wordTextInContext, paragraphText, bookTitle, bookGenre, bookDescription, bookAuthor, targetTranslationLanguage, userId, bookId, wordInstanceId);
        } catch (IOException e) {
            log.error("Échec de l'appel à l'IA pour l'instance '{}': {}", wordInstanceId, e.getMessage());
            throw e; // Relancer pour que le contrôleur gère l'erreur
        }

        // --- 3. Traitement et Sauvegarde de la Réponse LLM ---
        try {
            String wordFromLlm = llmResponseJson.path("word").asText(null);
            if (wordFromLlm == null || !wordFromLlm.equals(wordTextInContext)) {
                log.error("Inadéquation du mot LLM ! Demandé '{}', analyse reçue pour '{}'. Annulation de la sauvegarde pour l'instance {}",
                        wordTextInContext, wordFromLlm, wordInstanceId);
                // Retourner un DTO basé sur la réponse LLM même si le mot est différent,
                // mais ne pas sauvegarder ces données potentiellement incorrectes dans le cache contextuel.
                // Le frontend pourrait afficher un avertissement.
                return convertToWordAnalysisDto(llmResponseJson, llmResponseJson.path("translation").asText(null), 
                                              wordTextInContext, null, targetTranslationLanguage);
            }

            String canonicalFormFromLlm = llmResponseJson.path("canonical_form").asText(null);
            // Fallback: utiliser jidar comme canonical_form si non fourni par le LLM
            final String canonicalForm;
            if (canonicalFormFromLlm == null || canonicalFormFromLlm.isBlank()) {
                canonicalForm = llmResponseJson.path("jidar").asText(null);
                log.debug("Using jidar '{}' as canonical_form fallback", canonicalForm);
            } else {
                canonicalForm = canonicalFormFromLlm;
            }
            String translationText = llmResponseJson.path("translation").asText(null);

            // Créer la chaîne JSON pour analysis_data (sans word et translation, qui sont gérés séparément)
            ObjectNode analysisDataNode = objectMapper.createObjectNode();
            analysisDataNode.put("type", llmResponseJson.path("type").asText("unknown"));
            analysisDataNode.set("root", llmResponseJson.path("root"));
            analysisDataNode.set("jidar", llmResponseJson.path("jidar"));     // Forme nue vocalisée
            analysisDataNode.set("masdar", llmResponseJson.path("masdar"));   // Nom d'action
            analysisDataNode.set("wazn", llmResponseJson.path("wazn"));       // Schème morphologique
            analysisDataNode.set("details", llmResponseJson.path("details"));
            analysisDataNode.put("canonical_form_from_llm", canonicalForm); // Stocker pour référence
            String analysisDataJsonString = objectMapper.writeValueAsString(analysisDataNode);

            com.backcover.model.WordAnalysis wordAnalysisEntity = null;
            if (canonicalForm != null && !canonicalForm.isBlank()) {
                // Si on a déjà une analyse existante (du cache), la réutiliser
                if (existingAnalysis != null && existingAnalysis.getDictionaryWordId() != null) {
                    // Mettre à jour l'analyse existante avec les nouveaux details
                    wordAnalysisEntity = findOrCreateWordAnalysisEntity(
                        existingAnalysis.getDictionaryWordId(), 
                        analysisDataJsonString, 
                        targetTranslationLanguage,
                        llmResponseJson.path("details")
                    );
                } else {
                    // Créer une nouvelle analyse
                    DictionaryWord dictionaryWord = dictionaryWordRepository.findByWordTextAndLanguageCode(canonicalForm, "ar")
                            .orElseGet(() -> {
                                DictionaryWord newWord = new DictionaryWord();
                                newWord.setWordText(canonicalForm);
                                newWord.setLanguageCode("ar");
                                return dictionaryWordRepository.save(newWord);
                            });
                    // Passer les details et la langue cible à la méthode
                    wordAnalysisEntity = findOrCreateWordAnalysisEntity(
                        dictionaryWord.getId(), 
                        analysisDataJsonString, 
                        targetTranslationLanguage,
                        llmResponseJson.path("details")
                    );
                }
            }

            // --- 4. Sauvegarde avec la nouvelle architecture ---
            if (translationText != null && !translationText.isBlank() && wordAnalysisEntity != null) {
                try {
                    // Créer ou trouver la traduction dans le référentiel
                    WordTranslation translation = translationContextService.findOrCreateTranslation(
                            wordAnalysisEntity.getId(), 
                            targetTranslationLanguage, 
                            translationText, 
                            0.85, // Score de confiance par défaut pour Gemini
                            "Gemini-2.0-Flash-Exp"
                    );
                    
                    // Créer ou trouver le contexte de paragraphe
                    var paragraphContext = translationContextService.findOrCreateParagraphContext(paragraphText);
                    
                    // Créer ou mettre à jour le contexte de mot
                    translationContextService.createOrUpdateWordContext(
                            bookId, pageNumber, wordInstanceId, wordTextInContext,
                            wordAnalysisEntity.getId(), translation, paragraphContext
                    );
                    
                    log.info("Contexte de mot sauvegardé avec nouvelle architecture pour l'instance '{}'", wordInstanceId);
                    
                } catch (Exception e) {
                    log.error("Erreur lors de la sauvegarde avec nouvelle architecture pour l'instance {}: {}", 
                            wordInstanceId, e.getMessage(), e);
                    // Fallback vers l'ancienne méthode en cas d'erreur
                    saveWithLegacyMethod(bookId, pageNumber, wordInstanceId, wordAnalysisEntity, 
                                       wordTextInContext, paragraphText, targetTranslationLanguage, translationText);
                }
            } else {
                log.warn("Sauvegarde ignorée pour l'instance '{}': traduction manquante ou WordAnalysis non créé.", wordInstanceId);
            }

            // Convertir la réponse complète de l'IA en WordAnalysisDto pour le frontend
            return convertToWordAnalysisDto(llmResponseJson, translationText, wordTextInContext, 
                                          wordAnalysisEntity, targetTranslationLanguage);

        } catch (JsonProcessingException e) {
            log.error("Erreur lors du traitement/sauvegarde de la réponse JSON LLM pour l'instance {}: {}", wordInstanceId, e.getMessage(), e);
            throw new IOException("Erreur interne lors du traitement de la réponse d'analyse.", e);
        } catch (Exception e) {
            log.error("Erreur BDD inattendue lors du traitement/sauvegarde de la réponse LLM pour l'instance {}: {}", wordInstanceId, e.getMessage(), e);
            // Ne pas jeter l'exception ici pour quand même retourner le résultat de l'IA si possible
            // Mais le frontend ne saura pas que la sauvegarde a échoué.
            // Il est préférable de relancer pour indiquer un problème serveur.
            throw new IOException("Erreur serveur lors de la sauvegarde de l'analyse.", e);
        }
    }

    // Renommer cette méthode pour clarifier qu'elle retourne l'ENTITÉ JPA
    private com.backcover.model.WordAnalysis findOrCreateWordAnalysisEntity(UUID dictionaryWordId, String analysisDataJsonString, String targetLanguageCode, JsonNode detailsForLanguage) {
        // Déduplication basée uniquement sur le dictionaryWordId (mot canonique)
        // Une seule analyse par mot canonique, partagée entre tous les contextes
        Optional<com.backcover.model.WordAnalysis> existingAnalysis = wordAnalysisRepository.findByDictionaryWordId(dictionaryWordId).stream()
                .findFirst();
                
        if (existingAnalysis.isPresent()) {
            // Mettre à jour l'analyse existante avec les details de la nouvelle langue
            com.backcover.model.WordAnalysis analysis = existingAnalysis.get();
            try {
                JsonNode currentData = objectMapper.readTree(analysis.getAnalysisData());
                JsonNode newData = objectMapper.readTree(analysisDataJsonString);
                ObjectNode updatedData = currentData.deepCopy();

                // Merger les champs linguistiques (jidar, masdar, wazn) depuis les nouvelles données
                // Ces champs peuvent être absents dans les anciennes analyses
                if (newData.has("jidar") && !newData.get("jidar").isNull()) {
                    updatedData.set("jidar", newData.get("jidar"));
                }
                if (newData.has("masdar") && !newData.get("masdar").isNull()) {
                    updatedData.set("masdar", newData.get("masdar"));
                }
                if (newData.has("wazn") && !newData.get("wazn").isNull()) {
                    updatedData.set("wazn", newData.get("wazn"));
                }

                // Créer ou mettre à jour details_by_language
                ObjectNode detailsByLanguage;
                if (updatedData.has("details_by_language") && updatedData.get("details_by_language").isObject()) {
                    detailsByLanguage = (ObjectNode) updatedData.get("details_by_language");
                } else {
                    detailsByLanguage = objectMapper.createObjectNode();
                    // Migrer l'ancien details si présent
                    if (updatedData.has("details")) {
                        // Supposer que l'ancien details était en français par défaut
                        detailsByLanguage.set("fr", updatedData.get("details"));
                        updatedData.remove("details");
                    }
                    updatedData.set("details_by_language", detailsByLanguage);
                }

                // Ajouter les details pour la nouvelle langue
                detailsByLanguage.set(targetLanguageCode, detailsForLanguage);

                // Sauvegarder l'analyse mise à jour
                analysis.setAnalysisData(objectMapper.writeValueAsString(updatedData));
                log.info("Updated WordAnalysis entity with details for language '{}' for dictionary word ID: {}", targetLanguageCode, dictionaryWordId);
                return wordAnalysisRepository.save(analysis);
                
            } catch (Exception e) {
                log.error("Error updating existing analysis with new language details: {}", e.getMessage(), e);
                return analysis; // Retourner l'analyse existante sans modification en cas d'erreur
            }
        } else {
            // Créer une nouvelle analyse avec la structure multilingue dès le début
            try {
                JsonNode dataNode = objectMapper.readTree(analysisDataJsonString);
                ObjectNode newDataNode = dataNode.deepCopy();
                
                // Créer details_by_language avec la première langue
                ObjectNode detailsByLanguage = objectMapper.createObjectNode();
                detailsByLanguage.set(targetLanguageCode, detailsForLanguage);
                newDataNode.set("details_by_language", detailsByLanguage);
                newDataNode.remove("details"); // Supprimer l'ancien format
                
                com.backcover.model.WordAnalysis newAnalysis = new com.backcover.model.WordAnalysis();
                newAnalysis.setDictionaryWordId(dictionaryWordId);
                newAnalysis.setAnalysisData(objectMapper.writeValueAsString(newDataNode));
                newAnalysis.setSource("Gemini-2.0-Flash-Exp");
                log.info("Created new WordAnalysis entity with multilingual structure for dictionary word ID: {}", dictionaryWordId);
                return wordAnalysisRepository.save(newAnalysis);
            } catch (Exception e) {
                log.error("Error creating new analysis with multilingual structure: {}", e.getMessage(), e);
                // Fallback à l'ancienne méthode
                com.backcover.model.WordAnalysis newAnalysis = new com.backcover.model.WordAnalysis();
                newAnalysis.setDictionaryWordId(dictionaryWordId);
                newAnalysis.setAnalysisData(analysisDataJsonString);
                newAnalysis.setSource("Gemini-2.0-Flash-Exp");
                return wordAnalysisRepository.save(newAnalysis);
            }
        }
    }

    // Méthode pour convertir le JsonNode de l'IA (ou de la DB) en WordAnalysisDto
    private WordAnalysisDto convertToWordAnalysisDto(JsonNode analysisSourceNode, String translation, String originalWord, 
                                                    com.backcover.model.WordAnalysis wordAnalysisEntity, String targetLanguageCode) throws JsonProcessingException {
        WordAnalysisDto dto = new WordAnalysisDto();
        
        // AJOUTÉ : Set l'ID de l'analyse si disponible
        if (wordAnalysisEntity != null) {
            dto.setId(wordAnalysisEntity.getId());
            log.debug("Setting analysis ID {} in DTO for word '{}'", wordAnalysisEntity.getId(), originalWord);
        } else {
            log.debug("No analysis entity provided, DTO will not have analysis ID for word '{}'", originalWord);
        }
        
        dto.setWord(originalWord); // Toujours utiliser le mot original cliqué
        dto.setType(analysisSourceNode.path("type").asText("unknown"));
        dto.setTranslation(translation); // La traduction peut venir d'un champ séparé
        dto.setJidar(analysisSourceNode.path("jidar").asText(null));
        dto.setRoot(analysisSourceNode.path("root").asText(null));
        dto.setMasdar(analysisSourceNode.path("masdar").asText(null));
        dto.setWazn(analysisSourceNode.path("wazn").asText(null));
        
        // Extraire les details de la bonne langue
        JsonNode detailsNode = null;
        if (analysisSourceNode.has("details_by_language")) {
            // Nouveau format multilingue
            JsonNode detailsByLang = analysisSourceNode.get("details_by_language");
            if (detailsByLang.has(targetLanguageCode)) {
                detailsNode = detailsByLang.get(targetLanguageCode);
            } else if (detailsByLang.has("fr")) {
                // Fallback au français si la langue demandée n'est pas disponible
                detailsNode = detailsByLang.get("fr");
                log.warn("Details not available for language '{}', falling back to French", targetLanguageCode);
            } else {
                // Prendre la première langue disponible
                Iterator<String> fieldNames = detailsByLang.fieldNames();
                if (fieldNames.hasNext()) {
                    String firstLang = fieldNames.next();
                    detailsNode = detailsByLang.get(firstLang);
                    log.warn("Details not available for language '{}', falling back to '{}'", targetLanguageCode, firstLang);
                }
            }
        } else if (analysisSourceNode.has("details")) {
            // Ancien format - supposer que c'est en français
            detailsNode = analysisSourceNode.get("details");
            if (!"fr".equals(targetLanguageCode)) {
                log.warn("Using French details for requested language '{}'", targetLanguageCode);
            }
        }
        
        dto.setDetails(detailsNode); // Peut être null si aucun details trouvé
        dto.setCanonicalForm(analysisSourceNode.path("canonical_form").asText(
            analysisSourceNode.path("canonical_form_from_llm").asText(null)
        )); // Essayer les deux noms possibles
        
        // Parse lexical fields
        List<String> lexicalFields = new ArrayList<>();
        JsonNode lexicalFieldsNode = analysisSourceNode.path("lexical_fields");
        if (lexicalFieldsNode.isArray()) {
            for (JsonNode fieldNode : lexicalFieldsNode) {
                lexicalFields.add(fieldNode.asText());
            }
        }
        dto.setLexicalFields(lexicalFields);
        
        // Translate lexical fields to all languages
        Map<String, List<String>> translatedFields = lexicalFieldTranslationService.translateToAllLanguages(lexicalFields);
        dto.setLexicalFieldsTranslated(translatedFields);
        
        return dto;
    }


    // callGeminiForAnalysis, buildWordAnalysisPrompt, parseAndExtractJsonResponse restent des méthodes privées
    // et leur logique interne ne change pas fondamentalement, sauf pour s'assurer
    // que le JSON retourné par parseAndExtractJsonResponse contient bien tous les champs attendus par convertToWordAnalysisDto.

    private JsonNode callGeminiForAnalysis(String word, String paragraphText, String bookTitle, String bookGenre, String bookDescription, String bookAuthor, String targetLanguageCode, UUID userId, UUID bookId, String wordInstanceId) throws IOException {
        log.debug("Appel à l'API Gemini pour le mot : '{}' [Livre : '{}', Genre: '{}', Auteur: '{}'] avec langue cible '{}'", word, bookTitle, bookGenre, bookAuthor, targetLanguageCode);
        
        // Get prompt template for tracking
        PromptTemplate promptTemplate = promptTemplateRepository
                .findByIdentifierAndIsActiveTrue("WORD_ANALYSIS_V1")
                .orElse(null);
        
        if (promptTemplate == null) {
            log.warn("No active prompt template found for WORD_ANALYSIS_V1");
        }

        String requestJson = buildWordAnalysisPrompt(word, paragraphText, bookTitle, bookGenre, bookDescription, bookAuthor, targetLanguageCode);
        PromptUsageTracker.TrackingContext trackingContext = null;
        
        // Get dynamic model configuration for word analysis
        log.info("🔍 MODEL SELECTION - Looking for active model configuration for flow: WORD_ANALYSIS, provider: GOOGLE");
        String modelId = aiModelConfigService.getActiveModelForFlow(FlowType.WORD_ANALYSIS)
                .orElseThrow(() -> new ConfigurationNotFoundException(
                        "No active model configuration found for WORD_ANALYSIS flow with GOOGLE provider"));
        
        log.info("🎯 MODEL SELECTED - Using model: {} for word analysis (Provider: Gemini)", modelId);
        
        // Log prompt template details if available
        if (promptTemplate != null) {
            log.info("📝 PROMPT TEMPLATE - ID: {}", promptTemplate.getIdentifier());
        }
        
        // Start tracking if we have a prompt template
        if (promptTemplate != null) {
            trackingContext = promptUsageTracker.startTracking(
                promptTemplate, modelId, requestJson, userId, bookId, wordInstanceId);
        }
        
        // Construct URL dynamically with model from DB configuration
        String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";
        String dynamicApiUrl = baseUrl + modelId + ":generateContent";
        String urlWithKey = dynamicApiUrl + "?key=" + apiKey;
        
        // LOG THE ACTUAL API CALL
        log.info("🌐 GEMINI API CALL - Full URL: {}", dynamicApiUrl);
        log.info("🌐 GEMINI API CALL - Model from Config: {}", modelId);
        log.info("🌐 GEMINI API CALL - Request JSON: {}", requestJson);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(urlWithKey, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            
            // Extract token information from response (if available)
            int inputTokens = 0;
            int outputTokens = 0;
            try {
                JsonNode responseJson = objectMapper.readTree(responseBody);
                JsonNode usageMetadata = responseJson.path("usageMetadata");
                if (!usageMetadata.isMissingNode()) {
                    inputTokens = usageMetadata.path("promptTokenCount").asInt(0);
                    outputTokens = usageMetadata.path("candidatesTokenCount").asInt(0);
                    log.info("=== GEMINI WORD ANALYSIS TOKEN COUNT ===");
                    log.info("Prompt tokens: {}", inputTokens);
                    log.info("Response tokens: {}", outputTokens);
                    log.info("Total tokens: {}", inputTokens + outputTokens);
                    log.info("=== FIN TOKEN COUNT ===");
                }
            } catch (Exception tokenEx) {
                log.warn("Could not extract token usage from Gemini response: {}", tokenEx.getMessage());
            }
            
            JsonNode result = parseAndExtractJsonResponse(responseBody);
            
            // Complete tracking with success if we have a tracking context
            if (trackingContext != null) {
                promptUsageTracker.completeTracking(
                    trackingContext, 
                    responseBody, 
                    inputTokens, 
                    outputTokens, 
                    0.05, // temperature used in buildWordAnalysisPrompt
                    16384 // maxTokens used in buildWordAnalysisPrompt
                );
            }
            
            return result;
        } catch (RestClientException e) {
            log.error("Erreur réseau lors de l'appel à l'API Gemini pour le mot '{}': {}", word, e.getMessage());
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Network error: " + e.getMessage(), 0);
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new WordAnalysisException(WordAnalysisException.ErrorType.API_TIMEOUT, e);
            }
            throw new WordAnalysisException(WordAnalysisException.ErrorType.NETWORK_ERROR, e);
        } catch (JsonProcessingException e) {
            log.error("Erreur parsing réponse Gemini pour le mot '{}': {}", word, e.getMessage());
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Parsing error: " + e.getMessage(), 0);
            }
            throw new WordAnalysisException(WordAnalysisException.ErrorType.INVALID_RESPONSE, e);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'appel à l'API Gemini pour le mot '{}': {}", word, e.getMessage());
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Processing error: " + e.getMessage(), 0);
            }
            throw new WordAnalysisException(WordAnalysisException.ErrorType.INTERNAL_ERROR, e);
        }
    }

    private String buildWordAnalysisPrompt(String word, String paragraphText, String bookTitle, String bookGenre, String bookDescription, String bookAuthor, String targetLanguageCode) throws JsonProcessingException {
        // Valider et normaliser la langue
        String normalizedLanguage = languageConfig.validateAndNormalize(targetLanguageCode);
        log.debug("Building prompt for word '{}' in language '{}' (normalized: '{}')", word, targetLanguageCode, normalizedLanguage);

        // Nettoyer les paramètres pour éviter les problèmes d'échappement
        String cleanedWord = word != null ? word.trim() : "";
        String cleanedParagraphText = (paragraphText == null || paragraphText.isBlank()) ?
            "Aucun contexte de paragraphe fourni." : paragraphText.trim();
        String cleanedBookTitle = (bookTitle == null || bookTitle.isBlank()) ?
            "Inconnu" : bookTitle.trim();
        String cleanedBookGenre = (bookGenre == null || bookGenre.isBlank()) ?
            "Non spécifié" : bookGenre.trim();
        String cleanedBookDescription = (bookDescription == null || bookDescription.isBlank()) ?
            "Non disponible" : bookDescription.trim();
        String cleanedBookAuthor = (bookAuthor == null || bookAuthor.isBlank()) ?
            "Inconnu" : bookAuthor.trim();

        // Utiliser le service de templates pour générer le prompt
        String prompt = promptTemplateService.buildWordAnalysisPrompt(
            cleanedWord,
            cleanedBookTitle,
            cleanedBookGenre,
            cleanedBookDescription,
            cleanedBookAuthor,
            cleanedParagraphText,
            normalizedLanguage
        );

        // Construire le corps de la requête Gemini
        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode();
        
        part.put("text", prompt);
        content.set("parts", objectMapper.createArrayNode().add(part));
        requestBody.set("contents", objectMapper.createArrayNode().add(content));
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.4);
        generationConfig.put("maxOutputTokens", 6144); // Triplé pour éviter les troncatures
        requestBody.set("generationConfig", generationConfig);
        return objectMapper.writeValueAsString(requestBody);
    }

    private JsonNode parseAndExtractJsonResponse(String rawGeminiResponse) throws IOException {
        // ... (votre code de parsing existant, qui retourne le JsonNode interne de la réponse de Gemini) ...
        // Ce JsonNode devrait contenir word, type, translation, root, details, canonical_form
        if (rawGeminiResponse == null || rawGeminiResponse.isBlank()) { /* ... */ throw new IOException("Réponse vide."); }
        try {
            JsonNode root = objectMapper.readTree(rawGeminiResponse);
            
            // Extraire et logger le token count si disponible
            JsonNode usageMetadata = root.path("usageMetadata");
            if (!usageMetadata.isMissingNode()) {
                int promptTokenCount = usageMetadata.path("promptTokenCount").asInt(0);
                int candidatesTokenCount = usageMetadata.path("candidatesTokenCount").asInt(0);
                int totalTokenCount = usageMetadata.path("totalTokenCount").asInt(0);
                log.info("=== GEMINI TOKEN COUNT (Word Analysis) ===");
                log.info("Prompt tokens: {}", promptTokenCount);
                log.info("Response tokens: {}", candidatesTokenCount);
                log.info("Total tokens: {}", totalTokenCount);
                log.info("=== FIN TOKEN COUNT ===");
            }
            
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode()) { /* ... */ throw new IOException("Format réponse invalide."); }
            String generatedText = textNode.asText();
            if (generatedText.isBlank()) { /* ... */ throw new IOException("Réponse texte IA vide."); }
            String cleanedJsonText = generatedText.trim().replaceAll("^```json|```$", "").trim();
            return objectMapper.readTree(cleanedJsonText);
        } catch (JsonProcessingException e) { /* ... */ throw new IOException("Erreur parsing JSON.", e); }
    }

    /**
     * Méthode fallback pour sauvegarder avec l'ancienne architecture en cas d'erreur
     */
    private void saveWithLegacyMethod(UUID bookId, Integer pageNumber, String wordInstanceId,
                                    com.backcover.model.WordAnalysis wordAnalysisEntity,
                                    String wordTextInContext, String paragraphText,
                                    String targetTranslationLanguage, String translationText) {
        try {
            Optional<ContextualWordMeaning> existingMeaningAgain = contextualWordMeaningRepository
                    .findByBookIdAndPageNumberAndWordInstanceIdAndTranslationLanguageCode(
                            bookId, pageNumber, wordInstanceId, targetTranslationLanguage);

            if (existingMeaningAgain.isEmpty()) {
                ContextualWordMeaning newMeaning = new ContextualWordMeaning();
                newMeaning.setBookId(bookId);
                newMeaning.setPageNumber(pageNumber);
                newMeaning.setWordInstanceId(wordInstanceId);
                newMeaning.setWordAnalysis(wordAnalysisEntity);
                newMeaning.setWordTextInContext(wordTextInContext);
                newMeaning.setParagraphText(paragraphText);
                newMeaning.setTranslationLanguageCode(targetTranslationLanguage);
                newMeaning.setTranslationText(translationText);
                contextualWordMeaningRepository.save(newMeaning);
                log.info("Fallback: Signification contextuelle sauvegardée avec ancienne méthode pour l'instance '{}'", wordInstanceId);
            }
        } catch (Exception e) {
            log.error("Erreur même avec la méthode fallback pour l'instance {}: {}", wordInstanceId, e.getMessage(), e);
        }
    }
}