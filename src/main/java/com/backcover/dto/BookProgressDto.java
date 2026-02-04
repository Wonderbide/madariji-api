package com.backcover.dto;

import com.backcover.model.BookStatus;

public class BookProgressDto {
    private Integer currentPage;
    private Integer totalPages;
    private Integer progressPercentage;
    private BookStatus status;
    private String currentStep;
    private Integer currentBatch;
    private Integer totalBatches;
    private String processingDetails;

    public BookProgressDto() {}

    public BookProgressDto(Integer currentPage, Integer totalPages, BookStatus status, String currentStep) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.status = status;
        this.currentStep = currentStep;
        this.progressPercentage = calculateProgressPercentage();
        this.currentBatch = calculateCurrentBatch();
        this.totalBatches = calculateTotalBatches();
    }

    private Integer calculateProgressPercentage() {
        if (totalPages == null || totalPages == 0 || currentPage == null) {
            return 0;
        }
        return Math.min(100, (currentPage * 100) / totalPages);
    }

    private Integer calculateCurrentBatch() {
        if (currentPage == null) return null;
        return (currentPage / 100) + 1; // Batch commence à 1
    }

    private Integer calculateTotalBatches() {
        if (totalPages == null || totalPages == 0) return null;
        return (int) Math.ceil(totalPages / 100.0);
    }

    // Getters and Setters
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { 
        this.currentPage = currentPage;
        this.progressPercentage = calculateProgressPercentage();
        this.currentBatch = calculateCurrentBatch();
    }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { 
        this.totalPages = totalPages;
        this.progressPercentage = calculateProgressPercentage();
        this.totalBatches = calculateTotalBatches();
    }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public Integer getCurrentBatch() { return currentBatch; }
    public void setCurrentBatch(Integer currentBatch) { this.currentBatch = currentBatch; }

    public Integer getTotalBatches() { return totalBatches; }
    public void setTotalBatches(Integer totalBatches) { this.totalBatches = totalBatches; }

    public String getProcessingDetails() { return processingDetails; }
    public void setProcessingDetails(String processingDetails) { this.processingDetails = processingDetails; }

    @Override
    public String toString() {
        return "BookProgressDto{" +
                "currentPage=" + currentPage +
                ", totalPages=" + totalPages +
                ", progressPercentage=" + progressPercentage +
                ", status=" + status +
                ", currentStep='" + currentStep + '\'' +
                ", currentBatch=" + currentBatch +
                ", totalBatches=" + totalBatches +
                '}';
    }
}