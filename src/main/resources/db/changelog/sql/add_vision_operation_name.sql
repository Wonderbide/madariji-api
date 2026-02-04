-- Add vision_operation_name column to track Vision API operations
-- Author: Batch Processing Module
-- Date: 2025-08-19

ALTER TABLE batch_book 
ADD COLUMN IF NOT EXISTS vision_operation_name VARCHAR(255);

COMMENT ON COLUMN batch_book.vision_operation_name IS 'Google Vision API operation name for tracking OCR status';