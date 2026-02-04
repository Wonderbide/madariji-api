-- Migration pour créer la table batch_book pour le système de batch processing isolé
-- Author: Batch Processing Module
-- Date: 2025-08-11

-- Table simplifiée pour les livres traités par batch
CREATE TABLE IF NOT EXISTS batch_book (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    pdf_path TEXT,
    vision_path TEXT,
    results_path TEXT,
    total_pages INTEGER,
    processed_pages INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    openai_batch_id VARCHAR(100),
    openai_file_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vision_completed_at TIMESTAMP,
    batch_started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    metadata JSONB
);

-- Index pour les recherches fréquentes
CREATE INDEX idx_batch_book_status ON batch_book(status);
CREATE INDEX idx_batch_book_created_at ON batch_book(created_at DESC);
CREATE INDEX idx_batch_book_openai_batch_id ON batch_book(openai_batch_id);

-- Commentaires
COMMENT ON TABLE batch_book IS 'Table simplifiée pour le système de batch processing isolé';
COMMENT ON COLUMN batch_book.status IS 'UPLOADED, VISION_PROCESSING, VISION_READY, BATCH_PROCESSING, COMPLETED, FAILED';
COMMENT ON COLUMN batch_book.metadata IS 'Données additionnelles en JSON (stats, config, etc.)';