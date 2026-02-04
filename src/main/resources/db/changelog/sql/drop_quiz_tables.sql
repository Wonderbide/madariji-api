-- Drop quiz-related tables and their constraints
-- Drop foreign key constraints first
ALTER TABLE IF EXISTS user_word_learning_state DROP CONSTRAINT IF EXISTS FKm0duytjiee15ot0c35h5dmswi;
ALTER TABLE IF EXISTS user_word_learning_state DROP CONSTRAINT IF EXISTS FKgubh9ktv30xqy52rsll9okk4f;
ALTER TABLE IF EXISTS user_quiz_configuration DROP CONSTRAINT IF EXISTS FKpsyrb374st7q0y4f6ypibx12j;
ALTER TABLE IF EXISTS user_quiz_configuration_book_ids DROP CONSTRAINT IF EXISTS FK20t8dmc8eo7i48kv5vj1n7h2q;
ALTER TABLE IF EXISTS user_quiz_configuration_word_type_filters DROP CONSTRAINT IF EXISTS FKml68k8s5hor2jal16600d7q6y;

-- Drop indexes if any exist
DROP INDEX IF EXISTS idx_user_word_learning_state_user_id;
DROP INDEX IF EXISTS idx_user_word_learning_state_word_analysis_id;
DROP INDEX IF EXISTS idx_user_word_learning_state_next_review;
DROP INDEX IF EXISTS idx_user_quiz_configuration_user_id;

-- Drop the tables
DROP TABLE IF EXISTS user_quiz_configuration_word_type_filters CASCADE;
DROP TABLE IF EXISTS user_quiz_configuration_book_ids CASCADE;
DROP TABLE IF EXISTS user_word_learning_state CASCADE;
DROP TABLE IF EXISTS user_quiz_configuration CASCADE;