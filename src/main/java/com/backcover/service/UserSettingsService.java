package com.backcover.service; // ou sous-package approprié

import com.backcover.dto.user.UserSettingsDto;
import com.backcover.model.User;
import com.backcover.model.settings.UserSettings;
import com.backcover.repository.UserRepository;
import com.backcover.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserSettingsService(UserSettingsRepository userSettingsRepository, UserRepository userRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSettingsDto getUserSettings(String supabaseUserId) {
        User user = userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new RuntimeException("User not found with Supabase ID: " + supabaseUserId)); // Remplacer par une exception personnalisée plus tard

        UserSettings settings = findOrCreateUserSettings(user);
        return UserSettingsDto.fromEntity(settings);
    }

    public UserSettingsDto updateUserSettings(String supabaseUserId, UserSettingsDto settingsDto) {
        User user = userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new RuntimeException("User not found with Supabase ID: " + supabaseUserId));

        UserSettings settings = findOrCreateUserSettings(user);

        if (settingsDto.getReadingTheme() != null) {
            settings.setReadingTheme(settingsDto.getReadingTheme());
        }
        // Mise à jour des autres paramètres ici

        UserSettings updatedSettings = userSettingsRepository.save(settings);
        return UserSettingsDto.fromEntity(updatedSettings);
    }

    // Cette méthode peut rester publique si utile ailleurs, ou privée si seulement utilisée ici
    public UserSettings findOrCreateUserSettings(User user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings(user);
                    return userSettingsRepository.save(newSettings);
                });
    }
}