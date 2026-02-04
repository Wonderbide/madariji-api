package com.backcover.model.settings;

import com.backcover.model.User; // Assurez-vous que le package de User est correct
import jakarta.persistence.*; // JPA imports

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Laisser Hibernate choisir la meilleure stratégie pour les UUID générés par la BDD
    // Ou GenerationType.UUID si vous voulez être plus explicite (JPA 2.2+)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid") // columnDefinition pour forcer type uuid si besoin
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Hibernate utilisera user_id par défaut
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_theme", nullable = false, length = 50) // length pour correspondre à VARCHAR(50)
    private ReadingTheme readingTheme;

    // Constructeur par défaut requis par JPA
    public UserSettings() {
        this.readingTheme = ReadingTheme.LIGHT; // Valeur par défaut au niveau de l'objet
    }

    // Constructeur pour initialisation facile avec l'utilisateur
    public UserSettings(User user) {
        this(); // Appelle le constructeur par défaut pour initialiser les valeurs par défaut
        this.user = user;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ReadingTheme getReadingTheme() {
        return readingTheme;
    }

    public void setReadingTheme(ReadingTheme readingTheme) {
        this.readingTheme = readingTheme;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSettings that = (UserSettings) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", readingTheme=" + readingTheme +
                '}';
    }
}