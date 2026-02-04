package com.backcover.service;

import com.backcover.service.ia.PageStructuringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Dynamic page structuring service - delegates to Gemini implementation
 */
@Service
@Primary
public class DynamicPageStructuringService implements PageStructuringService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPageStructuringService.class);

    private final PageStructuringService geminiService;

    public DynamicPageStructuringService(
            @Qualifier("geminiPageStructuringService") PageStructuringService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public PageStructureResult structurePageText(String rawPageText) {
        return structurePageText(rawPageText, null, null);
    }

    @Override
    public PageStructureResult structurePageText(String rawPageText, UUID userId, UUID bookId) {
        log.info("🔀 PAGE STRUCTURING - Using Gemini service");
        return geminiService.structurePageText(rawPageText, userId, bookId);
    }
}
