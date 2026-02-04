-- Make local_pdf_path nullable (no longer used - PDF stored in R2)
ALTER TABLE book ALTER COLUMN local_pdf_path DROP NOT NULL;
