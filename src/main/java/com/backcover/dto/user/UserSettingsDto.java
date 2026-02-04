package com.backcover.dto.user; // ou com.backcover.dto.settings

import com.backcover.model.settings.ReadingTheme; // Assurez-vous que le chemin de l'enum est correct
import com.backcover.model.settings.UserSettings;

// Pas de Lombok, donc constructeurs, getters, setters manuels
public class UserSettingsDto {

    private ReadingTheme readingTheme;
    // Les autres paramètres (fontFamily, fontSize, etc.) viendront ici

    public UserSettingsDto() {
    }

    public UserSettingsDto(ReadingTheme readingTheme) {
        this.readingTheme = readingTheme;
    }

    // Static factory method pour la conversion depuis l'entité
    public static UserSettingsDto fromEntity(UserSettings entity) {
        if (entity == null) {
            return null; // Ou retourner un DTO avec des valeurs par défaut si préférable
        }
        UserSettingsDto dto = new UserSettingsDto();
        dto.setReadingTheme(entity.getReadingTheme());
        // Mapper les autres champs ici quand ils seront ajoutés
        return dto;
    }

    public ReadingTheme getReadingTheme() {
        return readingTheme;
    }

    public void setReadingTheme(ReadingTheme readingTheme) {
        this.readingTheme = readingTheme;
    }

    // Pas besoin d'equals/hashCode/toString complexes pour ce DTO simple pour le moment
}