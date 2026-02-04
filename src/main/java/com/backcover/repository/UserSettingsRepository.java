package com.backcover.repository;

import com.backcover.model.settings.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    /**
     * Finds UserSettings by the User's ID.
     *
     * @param userId The ID of the User.
     * @return An Optional containing UserSettings if found, otherwise empty.
     */
    Optional<UserSettings> findByUserId(UUID userId);

    /**
     * Finds UserSettings by the User's Supabase ID.
     * This might be more convenient in some service layers.
     *
     * @param supabaseUserId The Supabase ID of the User.
     * @return An Optional containing UserSettings if found, otherwise empty.
     */
    }