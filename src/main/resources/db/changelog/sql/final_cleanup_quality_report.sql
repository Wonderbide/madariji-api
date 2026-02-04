-- Final cleanup to ensure quality_report table is completely removed
-- This handles cases where the table might have been recreated

-- Drop all foreign key constraints that might reference quality_report
ALTER TABLE IF EXISTS prompt_usage_log DROP CONSTRAINT IF EXISTS fk_usage_log_quality_report;

-- Drop all indexes
DROP INDEX IF EXISTS idx_quality_report_usage_log_id;
DROP INDEX IF EXISTS idx_quality_report_report_type;
DROP INDEX IF EXISTS idx_quality_report_severity;
DROP INDEX IF EXISTS idx_quality_report_created_at;
DROP INDEX IF EXISTS idx_quality_report_user_id;
DROP INDEX IF EXISTS idx_quality_report_status;
DROP INDEX IF EXISTS idx_usage_log_id;
DROP INDEX IF EXISTS idx_report_type;
DROP INDEX IF EXISTS idx_severity;
DROP INDEX IF EXISTS idx_qr_created_at;
DROP INDEX IF EXISTS idx_qr_user_id;

-- Drop the table
DROP TABLE IF EXISTS quality_report CASCADE;

-- Ensure word_count_statistics is also dropped
DROP TABLE IF EXISTS word_count_statistics CASCADE;