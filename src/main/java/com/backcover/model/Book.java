package com.backcover.model;

import jakarta.persistence.*;
import java.time.Instant; // Utiliser Instant pour les timestamps
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "book")
public class Book {
    @Id
    // L'ID est généré par la DB dans votre changelog (gen_random_uuid()),
    // donc pas besoin de @GeneratedValue ici pour ce type UUID DB généré.
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_image_path", nullable = true, length = 1024)
    private String coverImagePath;

    @Column(nullable = false)
    // Changer le type en Instant pour mieux gérer TIMESTAMP WITH TIME ZONE de la DB
    private Instant uploadedAt;

    @Column(name = "final_content_path", length = 1024)
    private String finalContentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BookStatus status = BookStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String processingDetails;

    @Column(name = "last_successfully_processed_page_index")
    private Integer lastSuccessfullyProcessedPageIndex = -1;

    @Column(length = 255, name = "vision_operation_name", nullable = true)
    private String visionOperationName;

    // --- NOUVEAU CHAMP : Relation vers l'utilisateur ---
    @ManyToOne(fetch = FetchType.LAZY) // Beaucoup de livres pour un utilisateur. Fetching lazy par défaut.
    @JoinColumn(name = "user_id", nullable = false) // Nom de la colonne FK dans la table 'book'. Correspond au changelog 14.
    private User user; // L'utilisateur propriétaire du livre
    // --- FIN NOUVEAU CHAMP ---

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status", nullable = false, length = 50) // Stocker l'enum comme String
    private BookVisibilityStatus visibilityStatus = BookVisibilityStatus.PRIVATE; // Par défaut, un livre uploadé est privé

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "description")
    private String description;

    @Column(name = "published_date_text", length = 50)
    private String publishedDateText;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "vision_batch_size")
    private Integer visionBatchSize = 100;

    @Column(name = "total_word_count")
    private Integer totalWordCount;

    // --- Constructeur par défaut (requis par JPA) ---
    public Book() {}

    // Ajoutez d'autres constructeurs si nécessaire

    // --- Getters and Setters ---

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPublishedDateText() { return publishedDateText; }
    public void setPublishedDateText(String publishedDateText) { this.publishedDateText = publishedDateText; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; } // Utile si l'ID est défini par l'application avant la sauvegarde

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public Instant getUploadedAt() { return uploadedAt; } // Getter pour Instant
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; } // Setter pour Instant

    public String getFinalContentPath() { return finalContentPath; }
    public void setFinalContentPath(String finalContentPath) { this.finalContentPath = finalContentPath; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public String getProcessingDetails() { return processingDetails; }
    public void setProcessingDetails(String processingDetails) { this.processingDetails = processingDetails; }

    public Integer getLastSuccessfullyProcessedPageIndex() { return lastSuccessfullyProcessedPageIndex; }
    public void setLastSuccessfullyProcessedPageIndex(Integer lastSuccessfullyProcessedPageIndex) { this.lastSuccessfullyProcessedPageIndex = lastSuccessfullyProcessedPageIndex; }

    public String getVisionOperationName() { return visionOperationName; }
    public void setVisionOperationName(String visionOperationName) { this.visionOperationName = visionOperationName; }

    // --- Getters et Setters pour le champ user ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    // --- Fin Getters/Setters user ---

    public BookVisibilityStatus getVisibilityStatus() {
        return visibilityStatus;
    }

    public void setVisibilityStatus(BookVisibilityStatus visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Integer getVisionBatchSize() { return visionBatchSize; }
    public void setVisionBatchSize(Integer visionBatchSize) { this.visionBatchSize = visionBatchSize; }

    // --- equals, hashCode, toString ---
    // Recommandé de les baser sur l'ID de la DB pour les entités persistées.
    // Votre implémentation actuelle est correcte pour gérer les IDs nulls avant persistance.
    // Nous pouvons la garder, mais la baser UNIQUEMENT sur l'ID est la pratique la plus courante et souvent la plus sûre APRES la première sauvegarde.
    // Pour le MVP, basons equals/hashCode sur l'ID car c'est la clé unique et générée par la DB.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        // Basé sur l'ID UUID généré par la DB
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        // Basé sur l'ID UUID
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", visibilityStatus=" + visibilityStatus + // Inclure dans toString
                '}';
    }

}