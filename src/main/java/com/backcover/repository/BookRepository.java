package com.backcover.repository;

import com.backcover.model.Book; // Assurez-vous que l'import pointe vers votre package model/entity
import com.backcover.model.BookVisibilityStatus;
import com.backcover.model.User; // Importez l'entité User
import com.backcover.model.BookStatus;
// Importez PageImage si elle est utilisée ailleurs dans ce repository, sinon vous pouvez retirer l'import.
// import com.backcover.model.PageImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    // Méthodes de recherche existantes
    Optional<Book> findByTitle(String title);
    List<Book> findByStatus(BookStatus status);

    // NOUVELLE MÉTHODE : Trouver tous les livres appartenant à un utilisateur spécifique
    List<Book> findByUser(User user);

    // NOUVELLE MÉTHODE : Trouver un livre par son ID ET s'assurer qu'il appartient à un utilisateur spécifique
    // C'est crucial pour la sécurité (autorisation) sur les endpoints de détail, structure, etc.
    Optional<Book> findByIdAndUser(UUID id, User user);
    // Dans BookRepository.java
    List<Book> findByVisibilityStatus(BookVisibilityStatus visibilityStatus);

    // Pour le recovery service: trouver les livres dans plusieurs statuts
    List<Book> findByStatusIn(List<BookStatus> statuses);

    // Les méthodes CRUD standard (save, findById, etc.) sont héritées de JpaRepository
}