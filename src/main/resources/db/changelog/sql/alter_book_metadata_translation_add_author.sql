-- Add author column to book_metadata_translation table for transliterated author names
ALTER TABLE book_metadata_translation ADD COLUMN IF NOT EXISTS author VARCHAR(255);
