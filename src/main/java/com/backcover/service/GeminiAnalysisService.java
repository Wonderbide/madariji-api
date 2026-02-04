// Dans com/backcover/service/GeminiAnalysisService.java
package com.backcover.service; // ou com.backcover.service.ia.impl si vous le déplacez

import com.backcover.dto.TextBlockDto;
import com.backcover.model.prompt.PromptTemplate;
import com.backcover.repository.PromptTemplateRepository;
import com.backcover.service.ia.PageStructuringService; // <<< IMPORTER L'INTERFACE
import com.backcover.service.prompt.PromptUsageTracker;
import com.backcover.service.AIModelConfigurationService;
import com.backcover.model.AIFlowConfiguration.FlowType;
import com.backcover.exception.ConfigurationNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier; // <<< AJOUTER SI NÉCESSAIRE PLUS TARD
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service("geminiPageStructuringService") // Donner un nom qualifié au bean
public class GeminiAnalysisService implements PageStructuringService { // <<< IMPLÉMENTER L'INTERFACE

    private static final Logger log = LoggerFactory.getLogger(GeminiAnalysisService.class);
    @Value("${gemini.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PromptUsageTracker promptUsageTracker;
    private final PromptTemplateRepository promptTemplateRepository;
    private final AIModelConfigurationService aiModelConfigService;

    public GeminiAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                PromptUsageTracker promptUsageTracker, 
                                PromptTemplateRepository promptTemplateRepository,
                                AIModelConfigurationService aiModelConfigService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.promptUsageTracker = promptUsageTracker;
        this.promptTemplateRepository = promptTemplateRepository;
        this.aiModelConfigService = aiModelConfigService;
    }

    @Override // <<< Implémenter la méthode de l'interface
    public PageStructureResult structurePageText(String rawOcrText) { // <<< NOM ET TYPE DE RETOUR MIS À JOUR
        return structurePageText(rawOcrText, null, null);
    }

    @Override // <<< Implement interface method with tracking
    public PageStructureResult structurePageText(String rawOcrText, UUID userId, UUID bookId) {
        // Get dynamic model configuration first for logging
        String modelId = null;
        try {
            modelId = aiModelConfigService.getActiveModelForFlow(FlowType.PAGE_STRUCTURING)
                    .orElse("gemini-2.0-flash-exp");
        } catch (Exception e) {
            modelId = "gemini-2.0-flash-exp";
            log.warn("Could not get model configuration, using default: {}", modelId);
        }
        
        log.info("🤖 LLM CALL START - Model: {} | Task: PAGE_STRUCTURING | Input length: {} chars", 
                 modelId, rawOcrText != null ? rawOcrText.length() : 0);
        
        if (rawOcrText == null || rawOcrText.isBlank()) {
            log.warn("🤖 LLM CALL SKIPPED - Model: {} | Reason: Empty input text");
            return new PageStructureResult(false, Collections.emptyList());
        }

        // Get prompt template for PAGE_STRUCTURING_V5 (single version, no cascade)
        PromptTemplate promptTemplate = promptTemplateRepository
                .findByIdentifierAndIsActiveTrue("PAGE_STRUCTURING_V5")
                .orElse(null);

        if (promptTemplate == null) {
            log.error("CRITICAL: PAGE_STRUCTURING_V5 prompt not found in database");
            throw new IllegalStateException("PAGE_STRUCTURING_V5 prompt not found. Database configuration required.");
        }

        String requestJson = null;
        PromptUsageTracker.TrackingContext trackingContext = null;
        
        // modelId already retrieved at the beginning of the method
        // Ensure we have a valid modelId
        if (modelId == null) {
            throw new ConfigurationNotFoundException(
                    "No active model configuration found for PAGE_STRUCTURING flow with GOOGLE provider");
        }
        
        try {
            // Use prompt from database only (NO FALLBACK policy)
            if (promptTemplate == null || promptTemplate.getPromptContent() == null || promptTemplate.getPromptContent().isBlank()) {
                log.error("CRITICAL: No active prompt template found for PAGE_STRUCTURING. NO FALLBACK ALLOWED.");
                throw new IllegalStateException("No active prompt template found for page structuring. Database configuration required.");
            }
            
            requestJson = buildGeminiRequestWithPrompt(rawOcrText, promptTemplate.getPromptContent());
            log.info("📝 Using prompt from DB - Template: {} v{}", promptTemplate.getIdentifier(), promptTemplate.getVersion());
            
            log.debug("Using model: {} for page structuring", modelId);
            
            // Log prompt template details if available
            if (promptTemplate != null) {
                log.info("📝 PROMPT TEMPLATE - ID: {}, Version: {}", 
                    promptTemplate.getIdentifier(), 
                    promptTemplate.getVersion());
            }
            
            // Start tracking if we have a prompt template
            if (promptTemplate != null) {
                trackingContext = promptUsageTracker.startTracking(
                    promptTemplate, modelId, requestJson, userId, bookId, null);
            }
        } catch (Exception e) {
            log.error("🤖 LLM CALL ERROR - Model: {} | Task: PAGE_STRUCTURING | Error building request: {}", modelId, e.getMessage(), e);
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, e.getMessage(), 0);
            }
            throw new RuntimeException("Erreur interne lors de la création de la requête Gemini.", e);
        }
        
        log.debug("Texte OCR brut reçu (longueur: {}). Début analyse Gemini.", rawOcrText.length());
        log.trace("Texte OCR brut à analyser : {}", rawOcrText);

        try {
            log.debug("Requête JSON pour Gemini construite.");
            log.trace("Corps de la requête JSON envoyé à Gemini : {}", requestJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            
            // Construct URL dynamically with model from DB configuration
            String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/";
            String dynamicApiUrl = baseUrl + modelId + ":generateContent";
            String urlWithKey = dynamicApiUrl + "?key=" + apiKey;
            
            // LOG THE ACTUAL API CALL
            log.info("🌐 GEMINI API CALL - Full URL: {}", dynamicApiUrl);
            log.info("🌐 GEMINI API CALL - Model from Config: {}", modelId);
            
            // Parse request JSON to get generation config
            try {
                JsonNode requestNode = objectMapper.readTree(requestJson);
                JsonNode genConfig = requestNode.path("generationConfig");
                if (!genConfig.isMissingNode()) {
                    log.info("🌐 GEMINI API CALL - Temperature: {}, MaxTokens: {}", 
                            genConfig.path("temperature").asDouble(), 
                            genConfig.path("maxOutputTokens").asInt());
                }
            } catch (Exception e) {
                log.debug("Could not parse generation config from request");
            }
            
            log.info("🌐 GEMINI API CALL - Request JSON: {}", requestJson);

            ResponseEntity<String> response = restTemplate.exchange(urlWithKey, HttpMethod.POST, entity, String.class);
            log.info("🤖 LLM RESPONSE - Model: {} | Status: {} | Response length: {} chars", 
                     modelId, response.getStatusCode(), response.getBody() != null ? response.getBody().length() : 0);

            String responseBody = response.getBody();
            log.trace("Corps de la réponse BRUTE reçu de Gemini : {}", responseBody);

            // Validation de la réponse avant parsing
            if (!isValidGeminiResponse(responseBody)) {
                log.error("Réponse Gemini invalide détectée. Structure JSON attendue non trouvée.");
                // Log temporaire pour debug
                if (responseBody != null) {
                    String preview = responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
                    log.error("Aperçu de la réponse Gemini invalide: {}", preview);
                }
                if (trackingContext != null) {
                    promptUsageTracker.completeTrackingWithError(trackingContext, "Invalid Gemini response structure", 0);
                }
                throw new RuntimeException("Réponse Gemini invalide: structure JSON attendue non trouvée");
            }

            // La méthode parseGeminiResponse devra maintenant retourner PageStructureResult
            PageStructureResult result = parseGeminiResponseToPageStructureResult(responseBody, trackingContext);
            log.info("🤖 LLM CALL SUCCESS - Model: {} | Task: PAGE_STRUCTURING | Result: keepPage={}, blocks={}", 
                     modelId, result.keepPage(), result.blocks().size());
            return result;

        } catch (HttpStatusCodeException e) {
            String responseBodyError = e.getResponseBodyAsString();
            log.error("🤖 LLM CALL ERROR - Model: {} | Task: PAGE_STRUCTURING | HTTP Error: {} | Response: {}", 
                     modelId, e.getStatusCode(), responseBodyError, e);
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "HTTP " + e.getStatusCode() + ": " + responseBodyError, 0);
            }
            throw new RuntimeException("Échec appel API Gemini: HTTP " + e.getStatusCode() + " - " + responseBodyError, e);
        } catch (RestClientException e) {
            log.error("🤖 LLM CALL ERROR - Model: {} | Task: PAGE_STRUCTURING | RestClient Error: {}", modelId, e.getMessage(), e);
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "RestClient error: " + e.getMessage(), 0);
            }
            throw new RuntimeException("Échec appel API Gemini: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("🤖 LLM CALL ERROR - Model: {} | Task: PAGE_STRUCTURING | Parse Error: {}", modelId, e.getMessage(), e);
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Parse error: " + e.getMessage(), 0);
            }
            throw new RuntimeException("Échec parsing réponse Gemini: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("🤖 LLM CALL ERROR - Model: {} | Task: PAGE_STRUCTURING | Unexpected Error: {}", modelId, e.getMessage(), e);
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Unexpected error: " + e.getMessage(), 0);
            }
            throw new RuntimeException("Échec analyse Gemini: " + e.getMessage(), e);
        }
    }

    // buildGeminiRequest reste inchangé
    private String buildGeminiRequest(String rawOcrText) throws JsonProcessingException {
        // ... (votre code existant) ...
        log.debug("Construction de la requête Gemini pour analyse OCR.");
        String cleanedOcrText = rawOcrText.replace("`", "");
        String revisedPrompt = """
        # ANALYSE DE PAGE OCR - STRUCTURATION JSON
        
        Transformez le texte OCR brut en structure JSON organisée.
        
        ## RÈGLES ESSENTIELLES
        1. **Préserver TOUS les mots** sans exception (même avec erreurs OCR)
        2. **Vocalisation arabe** (Tashkeel) si possible
        3. **Espacement "و"** : ajouter espace après si c'est la conjonction "et"
        4. **Reconstruire** les paragraphes logiques à partir des lignes fragmentées
        5. **Identifier** les éléments selon leur contexte (titres, notes, etc.)
        
        ## FORMAT JSON REQUIS
        ```json
        {
          "keep_page": true,
          "blocks": [
            {"block_type": "TYPE", "block_text": "CONTENU"}
          ]
        }
        ```
        
        ## TYPES DE BLOCS
        - `heading1`, `heading2` : Titres principaux et secondaires
        - `paragraph` : Paragraphes de texte
        - `list_item` : Éléments de liste
        - `footnote` : Notes de bas de page
        - `page_number` : Numéros de page
        - `header`, `footer` : En-têtes/pieds de page
        
        ## EXEMPLE
        OCR brut :
        ```
        الباب الأول
        في أصول الفقه
        
        قال المؤلف¹: العلم نور
        والجهل ظلام
        
        ¹ رحمه الله
        ```
        
        Résultat :
        ```json
        {
          "keep_page": true,
          "blocks": [
            {"block_type": "heading1", "block_text": "الباب الأول"},
            {"block_type": "heading2", "block_text": "في أصول الفقه"},
            {"block_type": "paragraph", "block_text": "قال المؤلف¹: العلم نور و الجهل ظلام"},
            {"block_type": "footnote", "block_text": "¹ رحمه الله"}
          ]
        }
        ```
        
        **TEXTE OCR À ANALYSER :**
        ```
        %s
        ```
        """;
        String finalPrompt = revisedPrompt.replace("%s", cleanedOcrText);
        log.info("🤖 LLM PROMPT - Model: {} | Task: PAGE_STRUCTURING | Prompt length: {} chars", finalPrompt.length());
        log.trace("Prompt final envoyé à Gemini (analyse OCR/structure) : {}", finalPrompt);
        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", finalPrompt);
        content.set("parts", objectMapper.createArrayNode().add(part));
        requestBody.set("contents", objectMapper.createArrayNode().add(content));
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.05); // Plus précis avec Gemini 2.5 Flash
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 16384); // Augmenté pour éviter les troncatures
        requestBody.set("generationConfig", generationConfig);
        
        // Ajout des safety settings pour plus de contrôle
        ObjectNode safetySettings = objectMapper.createObjectNode();
        safetySettings.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySettings.put("threshold", "BLOCK_NONE");
        requestBody.set("safetySettings", objectMapper.createArrayNode().add(safetySettings));
        String requestJsonString = objectMapper.writeValueAsString(requestBody);
        log.debug("Construction requête Gemini pour analyse OCR/structure terminée.");
        return requestJsonString;
    }
    
    private String buildGeminiRequestWithPrompt(String rawOcrText, String promptContent) throws JsonProcessingException {
        log.debug("Construction de la requête Gemini avec prompt depuis DB.");
        
        // Replace placeholder with OCR text
        String finalPrompt = promptContent.replace("{{ocr_text}}", rawOcrText);
        
        log.info("🤖 LLM PROMPT - Model: GEMINI | Task: PAGE_STRUCTURING | Prompt length: {} chars", finalPrompt.length());
        log.trace("Prompt final envoyé à Gemini (depuis DB) : {}", finalPrompt);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode content = objectMapper.createObjectNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", finalPrompt);
        content.set("parts", objectMapper.createArrayNode().add(part));
        requestBody.set("contents", objectMapper.createArrayNode().add(content));
        
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.05); // Plus précis avec Gemini 2.5 Flash
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 16384); // Augmenté pour éviter les troncatures
        requestBody.set("generationConfig", generationConfig);
        
        // Ajout des safety settings pour plus de contrôle
        ObjectNode safetySettings = objectMapper.createObjectNode();
        safetySettings.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySettings.put("threshold", "BLOCK_NONE");
        requestBody.set("safetySettings", objectMapper.createArrayNode().add(safetySettings));
        
        String requestJsonString = objectMapper.writeValueAsString(requestBody);
        log.debug("Construction requête Gemini avec prompt DB terminée.");
        return requestJsonString;
    }
    
    /**
     * Valide que la réponse Gemini contient la structure JSON attendue
     */
    private boolean isValidGeminiResponse(String response) {
        if (response == null || response.isBlank()) {
            return false;
        }
        
        try {
            // Extraire le JSON de la réponse Gemini
            String cleanedJsonText = extractJsonFromGeminiResponse(response);
            if (cleanedJsonText == null) {
                log.warn("Impossible d'extraire le JSON de la réponse Gemini");
                return false;
            }
            
            JsonNode json = objectMapper.readTree(cleanedJsonText);
            
            // Vérifier la présence des champs obligatoires
            if (!json.has("keep_page") || !json.has("blocks")) {
                log.warn("Réponse Gemini manque keep_page ou blocks");
                return false;
            }
            
            // Vérifier que blocks est un array
            if (!json.get("blocks").isArray()) {
                log.warn("Le champ 'blocks' n'est pas un array");
                return false;
            }
            
            // Vérifier que chaque bloc a les champs requis
            for (JsonNode block : json.get("blocks")) {
                if (!block.has("block_type") || !block.has("block_text")) {
                    log.warn("Bloc manque block_type ou block_text");
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("Erreur lors de la validation de la réponse Gemini: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrait le JSON de la réponse Gemini (méthode helper pour réutilisation)
     */
    private String extractJsonFromGeminiResponse(String response) {
        // Cette logique devrait être similaire à celle dans parseGeminiResponseToPageStructureResult
        // Pour éviter la duplication, on peut l'extraire ici
        try {
            JsonNode responseJson = objectMapper.readTree(response);
            
            // Vérifier si la réponse a été tronquée
            JsonNode candidates = responseJson.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode candidate = candidates.get(0);
                JsonNode finishReason = candidate.get("finishReason");
                if (finishReason != null && "MAX_TOKENS".equals(finishReason.asText())) {
                    log.warn("Réponse Gemini tronquée (MAX_TOKENS). La réponse dépasse la limite de tokens.");
                    return null;
                }
                JsonNode content = candidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).get("text").asText();
                        
                        // Extraire le JSON entre ```json et ```
                        int startIndex = text.indexOf("```json");
                        if (startIndex != -1) {
                            startIndex += 7; // Longueur de "```json"
                            int endIndex = text.indexOf("```", startIndex);
                            if (endIndex != -1) {
                                return text.substring(startIndex, endIndex).trim();
                            } else {
                                log.warn("Trouvé ```json mais pas de ``` de fermeture");
                            }
                        } else {
                            log.warn("Pas de ```json trouvé dans la réponse. Début du texte: {}", 
                                    text.length() > 100 ? text.substring(0, 100) + "..." : text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Erreur lors de l'extraction JSON: {}", e.getMessage());
        }
        return null;
    }

    // Renommer et adapter pour retourner PageStructureResult
    private PageStructureResult parseGeminiResponseToPageStructureResult(String jsonResponse, 
            Object trackingContext) throws IOException {
        log.debug("Parsing de la réponse Gemini pour PageStructureResult...");
        // ... (votre logique de parsing existante) ...
        // Assurez-vous que la fin de cette méthode retourne bien :
        // return new PageStructureResult(keepPage, blocks);

        // Copie de votre logique de parsing, adaptée pour le nouveau nom de retour
        if (jsonResponse == null || jsonResponse.isBlank()){
            log.error("Réponse JSON de Gemini vide ou nulle reçue.");
            throw new IOException("Réponse JSON de Gemini vide ou nulle.");
        }
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // Extraire et logger le token count si disponible
            JsonNode usageMetadata = root.path("usageMetadata");
            int promptTokenCount = 0;
            int candidatesTokenCount = 0;
            int totalTokenCount = 0;
            
            if (!usageMetadata.isMissingNode()) {
                promptTokenCount = usageMetadata.path("promptTokenCount").asInt(0);
                candidatesTokenCount = usageMetadata.path("candidatesTokenCount").asInt(0);
                totalTokenCount = usageMetadata.path("totalTokenCount").asInt(0);
                log.info("=== GEMINI TOKEN COUNT ===");
                log.info("Prompt tokens: {}", promptTokenCount);
                log.info("Response tokens: {}", candidatesTokenCount);
                log.info("Total tokens: {}", totalTokenCount);
                log.info("=== FIN TOKEN COUNT ===");
            }
            
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode()) {
                log.error("Structure de réponse Gemini inattendue. Réponse brute : {}", jsonResponse);
                throw new IOException("Format de réponse Gemini invalide: 'text' non trouvé.");
            }
            String geminiOutputText = textNode.asText();
            if (geminiOutputText.isBlank()) {
                throw new IOException("Réponse texte interne de Gemini vide.");
            }
            String cleanedJsonText = geminiOutputText.trim();
            if (cleanedJsonText.startsWith("```json")) {
                cleanedJsonText = cleanedJsonText.substring(7);
                if (cleanedJsonText.endsWith("```")) {
                    cleanedJsonText = cleanedJsonText.substring(0, cleanedJsonText.length() - 3);
                }
            } else if (cleanedJsonText.startsWith("```")) {
                cleanedJsonText = cleanedJsonText.substring(3);
                if (cleanedJsonText.endsWith("```")) {
                    cleanedJsonText = cleanedJsonText.substring(0, cleanedJsonText.length() - 3);
                }
            }
            cleanedJsonText = cleanedJsonText.trim();
            try {
                JsonNode analysisJson = objectMapper.readTree(cleanedJsonText);
                boolean keepPage = analysisJson.path("keep_page").asBoolean(false);
                List<TextBlockDto> blocks = new ArrayList<>();
                if (keepPage && analysisJson.path("blocks").isArray()) {
                    for (JsonNode blockNode : analysisJson.path("blocks")) {
                        String blockType = blockNode.path("block_type").asText(null);
                        String blockText = blockNode.path("block_text").asText(null);
                        if (blockType != null && !blockType.isBlank() && blockText != null) {
                            blocks.add(new TextBlockDto(blockType, blockText));
                        } else {
                            log.warn("Bloc JSON invalide ou incomplet ignoré: Type='{}', TexteIsNull? {}", blockType, blockText == null);
                        }
                    }
                }
                PageStructureResult result = new PageStructureResult(keepPage, blocks);
                
                // Complete tracking with success if we have a tracking context
                if (trackingContext != null) {
                    promptUsageTracker.completeTracking(
                        trackingContext, 
                        jsonResponse, 
                        promptTokenCount, 
                        candidatesTokenCount, 
                        0.05, // temperature used in buildGeminiRequest
                        16384 // maxTokens used in buildGeminiRequest
                    );
                }
                
                return result; // <<< RETOURNE LE BON TYPE
            } catch (JsonProcessingException e) {
                log.error("ÉCHEC parsing JSON interne après nettoyage ```. Texte essayé: [{}]. Erreur: {}", cleanedJsonText, e.getMessage());
                if (trackingContext != null) {
                    promptUsageTracker.completeTrackingWithError(trackingContext, "JSON parsing error: " + e.getMessage(), promptTokenCount);
                }
                throw new IOException("Impossible de parser la réponse JSON interne de Gemini.", e);
            }
        } catch (IOException e) {
            log.error("Erreur lors du traitement global de la réponse Gemini: {}", e.getMessage());
            if (trackingContext != null) {
                promptUsageTracker.completeTrackingWithError(trackingContext, "Response parsing error: " + e.getMessage(), 0);
            }
            throw e;
        }
    }
}