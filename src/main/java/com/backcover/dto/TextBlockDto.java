package com.backcover.dto;

import lombok.experimental.Accessors;

public record TextBlockDto(
        String blockType, // "header", "paragraph", "h1", etc.
        String blockText  // Texte AVEC Tashkeel
) {}