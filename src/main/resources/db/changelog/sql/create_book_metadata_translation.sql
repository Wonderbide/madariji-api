-- Create book_metadata_translation table
CREATE TABLE IF NOT EXISTS book_metadata_translation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    language_code VARCHAR(5) NOT NULL,
    title VARCHAR(500),
    genre VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(book_id, language_code)
);

CREATE INDEX IF NOT EXISTS idx_book_meta_trans_book_id ON book_metadata_translation(book_id);
CREATE INDEX IF NOT EXISTS idx_book_meta_trans_lang ON book_metadata_translation(language_code);
