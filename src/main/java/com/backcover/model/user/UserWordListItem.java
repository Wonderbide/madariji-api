package com.backcover.model.user;

import com.backcover.model.Book;
import com.backcover.model.User;
import com.backcover.model.WordAnalysis;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_word_list_item", uniqueConstraints = {
        @UniqueConstraint(name = "ux_userwordlistitem_list_book_wordinstance", 
                         columnNames = {"list_id", "book_id", "word_instance_id"})
})
public class UserWordListItem {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private UserWordList wordList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "word_instance_id", nullable = false, length = 100)
    private String wordInstanceId;

    @Column(name = "word_text", nullable = false, columnDefinition = "TEXT")
    private String wordText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_analysis_id")
    private WordAnalysis wordAnalysis;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false, nullable = false)
    private Instant addedAt;

    // Constructors
    public UserWordListItem() {
    }

    public UserWordListItem(UserWordList wordList, User user, Book book, Integer pageNumber, 
                           String wordInstanceId, String wordText, WordAnalysis wordAnalysis) {
        this.wordList = wordList;
        this.user = user;
        this.book = book;
        this.pageNumber = pageNumber;
        this.wordInstanceId = wordInstanceId;
        this.wordText = wordText;
        this.wordAnalysis = wordAnalysis;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserWordList getWordList() {
        return wordList;
    }

    public void setWordList(UserWordList wordList) {
        this.wordList = wordList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getWordInstanceId() {
        return wordInstanceId;
    }

    public void setWordInstanceId(String wordInstanceId) {
        this.wordInstanceId = wordInstanceId;
    }

    public String getWordText() {
        return wordText;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public WordAnalysis getWordAnalysis() {
        return wordAnalysis;
    }

    public void setWordAnalysis(WordAnalysis wordAnalysis) {
        this.wordAnalysis = wordAnalysis;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}