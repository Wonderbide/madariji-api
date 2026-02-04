-- liquibase formatted sql

-- changeset omar:2025-08-22-make-local-pdf-path-nullable
-- Rendre la colonne local_pdf_path nullable car les PDFs sont maintenant stockés uniquement dans R2
ALTER TABLE book ALTER COLUMN local_pdf_path DROP NOT NULL;

-- Commenter pour documentation
COMMENT ON COLUMN book.local_pdf_path IS 'Chemin local du PDF (deprecated - utilise R2 maintenant). Nullable depuis migration vers R2.';

-- rollback ALTER TABLE book ALTER COLUMN local_pdf_path SET NOT NULL;