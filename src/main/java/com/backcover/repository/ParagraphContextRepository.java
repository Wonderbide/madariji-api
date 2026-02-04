package com.backcover.repository;

import com.backcover.model.ParagraphContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParagraphContextRepository extends JpaRepository<ParagraphContext, String> {
    
    /**
     * Trouve un contexte de paragraphe par son hash
     */
    Optional<ParagraphContext> findByContextHash(String contextHash);
    
    /**
     * Vérifie si un contexte existe par son hash
     */
    boolean existsByContextHash(String contextHash);
}