package com.backcover.dto.book;

import com.backcover.model.BookStatus;
import java.time.Instant; // Utiliser Instant
// import java.time.LocalDateTime; // Supprimer l'import de LocalDateTime
import java.util.Objects;
import java.util.UUID;

public class BookSummaryDto {
    private UUID id;
    private String title;
    private BookStatus status;
    private Instant uploadedAt; // Changer le type en Instant
    private String processingDetails; // Utile pour voir rapidement l'état
    private String coverImageUrl;     // URL pour récupérer l'image de couverture
    private Integer lastReadPage;      // Dernière page lue par l'utilisateur
    private Integer totalPages;        // Nombre total de pages du livre
    private boolean hasBeenRead;       // Indicateur si le livre a été ouvert par l'utilisateur
    private String authorName;         // Auteur du livre
    private String genre;              // Genre du livre
    private String description;        // Description du livre
    private String publishedDateText;  // Date de publication

    // Constructeurs
    public BookSummaryDto() {}

    // Mettre à jour le type du paramètre dans le constructeur
    public BookSummaryDto(UUID id, String title, BookStatus status, Instant uploadedAt, String processingDetails, String coverImageUrl) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.uploadedAt = uploadedAt; // Assignation directe
        this.processingDetails = processingDetails;
        this.coverImageUrl = coverImageUrl;
    }
    
    // Nouveau constructeur avec informations de lecture
    public BookSummaryDto(UUID id, String title, BookStatus status, Instant uploadedAt, String processingDetails, 
                         String coverImageUrl, Integer lastReadPage, Integer totalPages, boolean hasBeenRead) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.processingDetails = processingDetails;
        this.coverImageUrl = coverImageUrl;
        this.lastReadPage = lastReadPage;
        this.totalPages = totalPages;
        this.hasBeenRead = hasBeenRead;
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public BookStatus getStatus() { return status; }
    public Instant getUploadedAt() { return uploadedAt; } // Retourne Instant
    public String getProcessingDetails() { return processingDetails; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Integer getLastReadPage() { return lastReadPage; }
    public Integer getTotalPages() { return totalPages; }
    public boolean isHasBeenRead() { return hasBeenRead; }
    public String getAuthorName() { return authorName; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getPublishedDateText() { return publishedDateText; }

    // Setters - Mettre à jour le type du paramètre
    public void setId(UUID id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setStatus(BookStatus status) { this.status = status; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; } // Accepte Instant
    public void setProcessingDetails(String processingDetails) { this.processingDetails = processingDetails; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public void setLastReadPage(Integer lastReadPage) { this.lastReadPage = lastReadPage; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setHasBeenRead(boolean hasBeenRead) { this.hasBeenRead = hasBeenRead; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDescription(String description) { this.description = description; }
    public void setPublishedDateText(String publishedDateText) { this.publishedDateText = publishedDateText; }

    // equals, hashCode, toString - Objects.equals et Objects.hash gèrent Instant
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookSummaryDto that = (BookSummaryDto) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && status == that.status && Objects.equals(uploadedAt, that.uploadedAt) && Objects.equals(processingDetails, that.processingDetails) && Objects.equals(coverImageUrl, that.coverImageUrl) && Objects.equals(lastReadPage, that.lastReadPage) && Objects.equals(totalPages, that.totalPages) && hasBeenRead == that.hasBeenRead;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, status, uploadedAt, processingDetails, coverImageUrl, lastReadPage, totalPages, hasBeenRead);
    }

    @Override
    public String toString() {
        return "BookSummaryDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", uploadedAt=" + uploadedAt +
                ", processingDetails='" + processingDetails + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", lastReadPage=" + lastReadPage +
                ", totalPages=" + totalPages +
                ", hasBeenRead=" + hasBeenRead +
                '}';
    }
}