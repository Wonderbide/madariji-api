package com.backcover.repository;

import com.backcover.model.prompt.ModelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelConfigurationRepository extends JpaRepository<ModelConfiguration, UUID> {
    
    /**
     * Find model configuration by model code
     */
    Optional<ModelConfiguration> findByModelCode(String modelCode);
    
    
    /**
     * Find models by provider
     */
    List<ModelConfiguration> findByProviderOrderByModelName(String provider);
}