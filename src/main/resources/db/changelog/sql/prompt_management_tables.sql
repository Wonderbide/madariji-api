-- Create prompt_template table for dynamic prompt management
CREATE TABLE IF NOT EXISTS prompt_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    prompt_content TEXT NOT NULL,
    default_model VARCHAR(50),
    version VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    category VARCHAR(50),
    target_language_code VARCHAR(10),
    input_schema TEXT,
    output_schema TEXT,
    temperature DECIMAL(3,2),
    max_tokens INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create model_configuration table for AI model settings
CREATE TABLE IF NOT EXISTS model_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_id VARCHAR(50) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    api_endpoint VARCHAR(500),
    cost_per_1k_input_tokens DECIMAL(10,6),
    cost_per_1k_output_tokens DECIMAL(10,6),
    max_context_tokens INTEGER,
    max_output_tokens INTEGER,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    default_temperature DECIMAL(3,2),
    supported_features TEXT,
    performance_tier VARCHAR(20),
    avg_response_time_ms INTEGER,
    success_rate DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create enhanced prompt_usage_log table
CREATE TABLE IF NOT EXISTS prompt_usage_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_template_id UUID REFERENCES prompt_template(id),
    prompt_identifier VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(20),
    llm_model VARCHAR(50) NOT NULL,
    input_tokens INTEGER NOT NULL,
    output_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    estimated_cost_usd DECIMAL(10,6) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    user_id UUID,
    book_id UUID REFERENCES book(id),
    word_instance_id VARCHAR(100),
    quality_score DECIMAL(3,2),
    has_quality_issue BOOLEAN NOT NULL DEFAULT false,
    request_payload TEXT,
    response_payload TEXT,
    environment VARCHAR(20),
    api_version VARCHAR(20),
    temperature DECIMAL(3,2),
    max_tokens INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create quality_report table for user feedback
CREATE TABLE IF NOT EXISTS quality_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usage_log_id UUID NOT NULL REFERENCES prompt_usage_log(id),
    report_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    user_description TEXT,
    problematic_content TEXT,
    suggested_correction TEXT,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    action_taken TEXT,
    led_to_prompt_update BOOLEAN NOT NULL DEFAULT false,
    context_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_prompt_usage_log_prompt_identifier ON prompt_usage_log(prompt_identifier);
CREATE INDEX IF NOT EXISTS idx_prompt_usage_log_llm_model ON prompt_usage_log(llm_model);
CREATE INDEX IF NOT EXISTS idx_prompt_usage_log_created_at ON prompt_usage_log(created_at);
CREATE INDEX IF NOT EXISTS idx_prompt_usage_log_user_id ON prompt_usage_log(user_id);
CREATE INDEX IF NOT EXISTS idx_prompt_usage_log_book_id ON prompt_usage_log(book_id);

CREATE INDEX IF NOT EXISTS idx_quality_report_usage_log_id ON quality_report(usage_log_id);
CREATE INDEX IF NOT EXISTS idx_quality_report_report_type ON quality_report(report_type);
CREATE INDEX IF NOT EXISTS idx_quality_report_severity ON quality_report(severity);
CREATE INDEX IF NOT EXISTS idx_quality_report_created_at ON quality_report(created_at);
CREATE INDEX IF NOT EXISTS idx_quality_report_user_id ON quality_report(user_id);

-- Insert initial model configurations (only if not exists)
INSERT INTO model_configuration (
    model_id, model_name, provider, 
    cost_per_1k_input_tokens, cost_per_1k_output_tokens,
    max_context_tokens, max_output_tokens,
    default_temperature, performance_tier,
    is_enabled
) VALUES 
(
    'gpt-4-turbo',
    'GPT-4 Turbo',
    'OpenAI',
    0.01, 0.03,
    128000, 4096,
    0.4, 'quality',
    true
),
(
    'gpt-3.5-turbo',
    'GPT-3.5 Turbo',
    'OpenAI',
    0.0005, 0.0015,
    16385, 4096,
    0.7, 'fast',
    true
),
(
    'gemini-pro',
    'Gemini Pro',
    'Google',
    0.00025, 0.0005,
    32000, 8192,
    0.05, 'balanced',
    true
)
ON CONFLICT (model_id) DO NOTHING;

-- Note: Initial prompt templates will be inserted by PromptDataInitializer on startup
-- This ensures prompts are always up-to-date with the latest version from the code