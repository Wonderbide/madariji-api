package com.backcover.service;

import com.backcover.dto.WordAnalysisDto;
import com.backcover.service.ia.DetailedWordAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

/**
 * Dynamic word analyzer service - delegates to Gemini implementation
 */
@Service
@Primary
public class DynamicWordAnalyzerService implements DetailedWordAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(DynamicWordAnalyzerService.class);

    private final DetailedWordAnalyzerService geminiService;

    public DynamicWordAnalyzerService(
            @Qualifier("geminiDetailedWordAnalyzer") DetailedWordAnalyzerService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public WordAnalysisDto analyzeWord(String wordTextInContext, String paragraphText, String bookTitle,
                                       UUID bookId, Integer pageNumber, String wordInstanceId, String targetLanguageCode)
            throws IOException, IllegalArgumentException {

        log.info("🔀 WORD ANALYSIS - Using Gemini service");
        return geminiService.analyzeWord(wordTextInContext, paragraphText, bookTitle,
                bookId, pageNumber, wordInstanceId, targetLanguageCode);
    }
}
