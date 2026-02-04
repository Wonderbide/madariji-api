package com.backcover.repository;

import com.backcover.model.prompt.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
    
    /**
     * Find active prompt template by identifier (maps to promptKey)
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.promptKey = :identifier AND p.isActive = true")
    Optional<PromptTemplate> findByIdentifierAndIsActiveTrue(@Param("identifier") String identifier);
    
    /**
     * Find all active templates for a category (category removed - return all active)
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.isActive = true ORDER BY p.version DESC")
    List<PromptTemplate> findByCategoryAndIsActiveTrueOrderByVersionDesc(String category);
    
    /**
     * Find all versions of a prompt by identifier (maps to promptKey)
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.promptKey = :identifier ORDER BY p.version DESC")
    List<PromptTemplate> findByIdentifierOrderByVersionDesc(@Param("identifier") String identifier);
    
    /**
     * Check if a prompt identifier exists (maps to promptKey)
     */
    @Query("SELECT COUNT(p) > 0 FROM PromptTemplate p WHERE p.promptKey = :identifier")
    boolean existsByIdentifier(@Param("identifier") String identifier);
    
    /**
     * Find all active prompt templates (order by promptKey since category removed)
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.isActive = true ORDER BY p.promptKey")
    List<PromptTemplate> findByIsActiveTrueOrderByCategory();
    
    /**
     * Count active prompt templates
     */
    long countByIsActiveTrue();
    
    /**
     * Find first active prompt template by identifier prefix (maps to promptKey)
     */
    @Query("SELECT p FROM PromptTemplate p WHERE p.promptKey LIKE :prefix% AND p.isActive = true ORDER BY p.version DESC LIMIT 1")
    Optional<PromptTemplate> findFirstByIdentifierStartingWithAndIsActiveTrueOrderByVersionDesc(@Param("prefix") String identifierPrefix);
}