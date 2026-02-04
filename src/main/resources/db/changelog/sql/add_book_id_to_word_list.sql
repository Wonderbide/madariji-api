-- Migration to add optional book_id to user_word_list table for book-specific lists
-- This allows creating word lists that are specific to a particular book
-- while maintaining backward compatibility with language-based lists (book_id = NULL)

-- Add the book_id column as optional (nullable) only if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'user_word_list' AND column_name = 'book_id') THEN
        ALTER TABLE user_word_list ADD COLUMN book_id UUID;
    END IF;
END $$;

-- Add foreign key constraint to reference the book table (only if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'fk_userwordlist_book') THEN
        ALTER TABLE user_word_list 
        ADD CONSTRAINT fk_userwordlist_book 
        FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create indexes only if they don't exist
CREATE INDEX IF NOT EXISTS idx_userwordlist_book_id ON user_word_list(book_id);
CREATE INDEX IF NOT EXISTS idx_userwordlist_user_book ON user_word_list(user_id, book_id);
CREATE INDEX IF NOT EXISTS idx_userwordlist_user_lang_book ON user_word_list(user_id, language_code, book_id);