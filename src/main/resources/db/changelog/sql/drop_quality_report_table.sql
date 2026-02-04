-- Drop quality_report table and related indexes
-- This table was used for tracking quality reports on prompt usage

-- Drop indexes first
DROP INDEX IF EXISTS idx_quality_report_usage_log_id;
DROP INDEX IF EXISTS idx_quality_report_report_type;
DROP INDEX IF EXISTS idx_quality_report_severity;
DROP INDEX IF EXISTS idx_quality_report_created_at;
DROP INDEX IF EXISTS idx_quality_report_user_id;
DROP INDEX IF EXISTS idx_quality_report_status;

-- Drop the table
DROP TABLE IF EXISTS quality_report CASCADE;

-- Also drop word_count_statistics table if it exists
DROP TABLE IF EXISTS word_count_statistics CASCADE;