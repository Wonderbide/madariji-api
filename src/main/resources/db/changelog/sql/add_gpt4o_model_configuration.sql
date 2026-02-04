-- Add GPT-4o model configuration

INSERT INTO model_configuration (
    id,
    model_id,
    model_name,
    provider,
    api_endpoint,
    cost_per_1k_input_tokens,
    cost_per_1k_output_tokens,
    max_context_tokens,
    max_output_tokens,
    is_enabled,
    default_temperature,
    supported_features,
    performance_tier,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'gpt-4o',
    'GPT-4 Omni',
    'OpenAI',
    'https://api.openai.com/v1/chat/completions',
    0.005,    -- $5 per 1M input tokens
    0.015,    -- $15 per 1M output tokens
    128000,   -- 128K context window
    16384,    -- 16K max output
    true,
    0.4,
    'vision,function_calling,json_mode',
    'quality',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (model_id) DO UPDATE SET
    model_name = EXCLUDED.model_name,
    cost_per_1k_input_tokens = EXCLUDED.cost_per_1k_input_tokens,
    cost_per_1k_output_tokens = EXCLUDED.cost_per_1k_output_tokens,
    max_context_tokens = EXCLUDED.max_context_tokens,
    max_output_tokens = EXCLUDED.max_output_tokens,
    updated_at = CURRENT_TIMESTAMP;

-- Add GPT-4o-mini as well (often used in production)
INSERT INTO model_configuration (
    id,
    model_id,
    model_name,
    provider,
    api_endpoint,
    cost_per_1k_input_tokens,
    cost_per_1k_output_tokens,
    max_context_tokens,
    max_output_tokens,
    is_enabled,
    default_temperature,
    supported_features,
    performance_tier,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'gpt-4o-mini',
    'GPT-4 Omni Mini',
    'OpenAI',
    'https://api.openai.com/v1/chat/completions',
    0.00015,  -- $0.15 per 1M input tokens
    0.0006,   -- $0.60 per 1M output tokens
    128000,   -- 128K context window
    16384,    -- 16K max output
    true,
    0.4,
    'vision,function_calling,json_mode',
    'fast',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (model_id) DO UPDATE SET
    model_name = EXCLUDED.model_name,
    cost_per_1k_input_tokens = EXCLUDED.cost_per_1k_input_tokens,
    cost_per_1k_output_tokens = EXCLUDED.cost_per_1k_output_tokens,
    max_context_tokens = EXCLUDED.max_context_tokens,
    max_output_tokens = EXCLUDED.max_output_tokens,
    updated_at = CURRENT_TIMESTAMP;