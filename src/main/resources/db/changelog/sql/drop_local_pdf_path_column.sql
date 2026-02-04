-- Drop local_pdf_path column (no longer used - PDF stored in R2 only)
ALTER TABLE book DROP COLUMN IF EXISTS local_pdf_path;
