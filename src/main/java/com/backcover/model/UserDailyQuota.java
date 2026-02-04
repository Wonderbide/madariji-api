package com.backcover.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_quota", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_daily_quota_user_date", columnNames = {"user_id", "quota_date"})
})
public class UserDailyQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quota_date", nullable = false)
    private LocalDate quotaDate;

    @Column(name = "word_consultations", nullable = false)
    private Integer wordConsultations = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public UserDailyQuota() {
    }

    public UserDailyQuota(User user, LocalDate quotaDate) {
        this.user = user;
        this.quotaDate = quotaDate;
        this.wordConsultations = 0;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (quotaDate == null) {
            quotaDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getQuotaDate() { return quotaDate; }
    public void setQuotaDate(LocalDate quotaDate) { this.quotaDate = quotaDate; }

    public Integer getWordConsultations() { return wordConsultations; }
    public void setWordConsultations(Integer wordConsultations) { this.wordConsultations = wordConsultations; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void incrementWordConsultations() {
        this.wordConsultations++;
    }
}
