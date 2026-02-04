package com.backcover.repository;

import com.backcover.model.BookMetadataTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookMetadataTranslationRepository extends JpaRepository<BookMetadataTranslation, UUID> {

    Optional<BookMetadataTranslation> findByBookIdAndLanguageCode(UUID bookId, String languageCode);

    List<BookMetadataTranslation> findByBookId(UUID bookId);

    void deleteByBookId(UUID bookId);
}
