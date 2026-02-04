package com.backcover.exception;

public class UnsupportedProviderException extends RuntimeException {
    
    public UnsupportedProviderException(String message) {
        super(message);
    }
    
    public UnsupportedProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}