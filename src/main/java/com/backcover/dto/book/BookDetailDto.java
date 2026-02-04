package com.backcover.dto.book;

import com.backcover.model.Book;
import com.backcover.model.BookStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BookDetailDto {
    private UUID id;
    private String title;
    private BookStatus status;
    private Instant uploadedAt;
    private String processingDetails;
    private String visionOperationName;
    private String finalContentPath;
    private Integer lastReadPageNumber;

    public BookDetailDto() {}

    public BookDetailDto(UUID id, String title, BookStatus status, Instant uploadedAt, String processingDetails, String visionOperationName, String finalContentPath, Integer lastReadPageNumber) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.processingDetails = processingDetails;
        this.visionOperationName = visionOperationName;
        this.finalContentPath = finalContentPath;
        this.lastReadPageNumber = lastReadPageNumber;
    }

    public static BookDetailDto fromEntity(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDetailDto(
                book.getId(),
                book.getTitle(),
                book.getStatus(),
                book.getUploadedAt(),
                book.getProcessingDetails(),
                book.getVisionOperationName(),
                book.getFinalContentPath(),
                null
        );
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public BookStatus getStatus() { return status; }
    public Instant getUploadedAt() { return uploadedAt; }
    public String getProcessingDetails() { return processingDetails; }
    public String getVisionOperationName() { return visionOperationName; }
    public String getFinalContentPath() { return finalContentPath; }
    public Integer getLastReadPageNumber() { return lastReadPageNumber; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setStatus(BookStatus status) { this.status = status; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setProcessingDetails(String processingDetails) { this.processingDetails = processingDetails; }
    public void setVisionOperationName(String visionOperationName) { this.visionOperationName = visionOperationName; }
    public void setFinalContentPath(String finalContentPath) { this.finalContentPath = finalContentPath; }
    public void setLastReadPageNumber(Integer lastReadPageNumber) { this.lastReadPageNumber = lastReadPageNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDetailDto that = (BookDetailDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                status == that.status &&
                Objects.equals(uploadedAt, that.uploadedAt) &&
                Objects.equals(processingDetails, that.processingDetails) &&
                Objects.equals(visionOperationName, that.visionOperationName) &&
                Objects.equals(finalContentPath, that.finalContentPath) &&
                Objects.equals(lastReadPageNumber, that.lastReadPageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, status, uploadedAt, processingDetails, visionOperationName, finalContentPath, lastReadPageNumber);
    }

    @Override
    public String toString() {
        return "BookDetailDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", uploadedAt=" + uploadedAt +
                ", processingDetails='" + processingDetails + '\'' +
                ", visionOperationName='" + visionOperationName + '\'' +
                ", finalContentPath='" + finalContentPath + '\'' +
                ", lastReadPageNumber=" + lastReadPageNumber +
                '}';
    }
}
