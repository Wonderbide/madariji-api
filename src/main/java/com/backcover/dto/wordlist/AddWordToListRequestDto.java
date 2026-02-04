package com.backcover.dto.wordlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddWordToListRequestDto {
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
    
    @NotNull(message = "Page number is required")
    private Integer pageNumber;
    
    @NotBlank(message = "Word instance ID is required")
    private String wordInstanceId;
    
    @NotBlank(message = "Word text is required")
    private String wordText;
    
    private UUID wordAnalysisId; // Optional
    
    @NotBlank(message = "Target language code is required")
    private String targetLanguageCode; // Required for translation

    // Constructors
    public AddWordToListRequestDto() {
    }

    public AddWordToListRequestDto(UUID bookId, Integer pageNumber, String wordInstanceId, String wordText) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordText = wordText;
    }

    public AddWordToListRequestDto(UUID bookId, Integer pageNumber, String wordInstanceId, String wordText, UUID wordAnalysisId) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordText = wordText;
        this.wordAnalysisId = wordAnalysisId;
    }

    // Getters and Setters
    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public String getWordText() {
        return wordText;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public UUID getWordAnalysisId() {
        return wordAnalysisId;
    }

    public void setWordAnalysisId(UUID wordAnalysisId) {
        this.wordAnalysisId = wordAnalysisId;
    }

    public String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    public void setTargetLanguageCode(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }
}