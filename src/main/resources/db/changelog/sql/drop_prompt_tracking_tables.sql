-- Drop prompt tracking tables that are no longer needed

-- Drop foreign key constraints first
ALTER TABLE IF EXISTS prompt_ab_test DROP CONSTRAINT IF EXISTS fk_ab_test_template;
ALTER TABLE IF EXISTS prompt_ab_test DROP CONSTRAINT IF EXISTS fk_ab_test_variant_a;
ALTER TABLE IF EXISTS prompt_ab_test DROP CONSTRAINT IF EXISTS fk_ab_test_variant_b;
ALTER TABLE IF EXISTS prompt_usage_log DROP CONSTRAINT IF EXISTS fk_usage_log_template;

-- Drop tables
DROP TABLE IF EXISTS prompt_ab_test CASCADE;
DROP TABLE IF EXISTS prompt_usage_log CASCADE;