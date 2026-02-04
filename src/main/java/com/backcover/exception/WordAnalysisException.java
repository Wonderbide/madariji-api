package com.backcover.exception;

import lombok.Getter;

/**
 * Exception for word analysis errors with user-friendly messages.
 */
@Getter
public class WordAnalysisException extends RuntimeException {

    public enum ErrorType {
        NETWORK_ERROR(503, true,
            "Le service d'analyse est temporairement indisponible. Veuillez réessayer dans quelques instants."),
        API_TIMEOUT(504, true,
            "L'analyse prend plus de temps que prévu. Veuillez réessayer."),
        INVALID_RESPONSE(502, true,
            "La réponse du service d'analyse est invalide. Veuillez réessayer."),
        PARSING_ERROR(500, false,
            "Erreur lors du traitement de l'analyse. Contactez le support si le problème persiste."),
        QUOTA_EXCEEDED(429, false,
            "Limite d'analyse atteinte. Passez à Premium pour un accès illimité."),
        INTERNAL_ERROR(500, false,
            "Une erreur interne s'est produite. Veuillez réessayer plus tard.");

        @Getter
        private final int httpStatus;
        @Getter
        private final boolean retryable;
        @Getter
        private final String userMessage;

        ErrorType(int httpStatus, boolean retryable, String userMessage) {
            this.httpStatus = httpStatus;
            this.retryable = retryable;
            this.userMessage = userMessage;
        }
    }

    private final ErrorType errorType;

    public WordAnalysisException(ErrorType errorType) {
        super(errorType.getUserMessage());
        this.errorType = errorType;
    }

    public WordAnalysisException(ErrorType errorType, Throwable cause) {
        super(errorType.getUserMessage(), cause);
        this.errorType = errorType;
    }
}
