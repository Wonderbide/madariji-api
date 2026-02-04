package com.backcover.model;

public enum BookStatus {
    PENDING,                // PDF uploadé, en attente de traitement initial.
    TEXT_EXTRACTION_IN_PROGRESS,        // NOUVEAU: Tâche OCR (extraction texte) lancée, en attente du résultat.
    PROCESSING,             // ANCIEN: A évaluer si on le garde ou supprime/renomme pour une autre étape ?
    PROCESSING_GEMINI,      // (Futur - à renommer aussi, ex: ENRICHMENT_IN_PROGRESS)
    COMPLETED,              // Traitement complet terminé avec succès.
    COMPLETED_WITH_ERRORS,  // (Optionnel)
    PAUSED_ON_ERROR,
    OCR_COMPLETED,// (Optionnel)
    FAILED,

    AWAITING_ENRICHMENT,

    PROCESSING_OCR_RESULTS,
    ENRICHMENT_IN_PROGRESS,
    PARTIALLY_ENRICHED
}