-- Insert initial prompts into ai_prompt_template if they don't exist
-- These prompts are currently referenced in the code

-- WORD_ANALYSIS_V1 prompt
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'WORD_ANALYSIS_V1',
    'You are a linguistics expert. Analyze the word "%s" in the context: "%s". 
    Provide a detailed analysis including:
    1. Translation to %s
    2. Root word and etymology
    3. Grammatical category
    4. Usage examples
    5. Related words
    Return the response in JSON format.',
    '1.0',
    true,
    'WORD_ANALYSIS',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'WORD_ANALYSIS_V1'
);

-- PAGE_STRUCTURING_V1 prompt
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'PAGE_STRUCTURING_V1',
    'You are a document structure analyzer. Analyze the following OCR text and structure it into pages.
    Text: %s
    
    Identify:
    1. Page breaks
    2. Headers and footers
    3. Paragraphs
    4. Main content vs metadata
    
    Return as structured JSON with page numbers and content.',
    '1.0',
    false,
    'PAGE_STRUCTURING',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'PAGE_STRUCTURING_V1'
);

-- PAGE_STRUCTURING_V2 prompt
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'PAGE_STRUCTURING_V2',
    'Analyze and structure this OCR text into pages. Text: %s
    Return JSON with page_number, content, and metadata.',
    '2.0',
    false,
    'PAGE_STRUCTURING',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'PAGE_STRUCTURING_V2'
);

-- PAGE_STRUCTURING_V3 prompt
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'PAGE_STRUCTURING_V3',
    'Structure OCR text: %s. Identify pages, paragraphs, headers. Return structured JSON.',
    '3.0',
    false,
    'PAGE_STRUCTURING',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'PAGE_STRUCTURING_V3'
);

-- PAGE_STRUCTURING_V4 prompt  
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'PAGE_STRUCTURING_V4',
    'Parse and structure document: %s. Extract pages with clear boundaries. Format as JSON.',
    '4.0',
    false,
    'PAGE_STRUCTURING',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'PAGE_STRUCTURING_V4'
);

-- PAGE_STRUCTURING_V5 prompt (Latest/Active)
INSERT INTO ai_prompt_template (
    id, 
    identifier, 
    prompt_content, 
    version, 
    is_active, 
    category,
    created_by,
    created_at,
    updated_at
) 
SELECT 
    gen_random_uuid(),
    'PAGE_STRUCTURING_V5',
    'Advanced document structuring: Analyze the OCR text and create a precise page-by-page structure.
    Input: %s
    
    Requirements:
    - Identify exact page boundaries
    - Preserve all content including headers/footers
    - Maintain paragraph structure
    - Detect page numbers
    
    Output: JSON with pages array containing page_number, content, paragraphs.',
    '5.0',
    true,
    'PAGE_STRUCTURING',
    'system',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM ai_prompt_template WHERE identifier = 'PAGE_STRUCTURING_V5'
);