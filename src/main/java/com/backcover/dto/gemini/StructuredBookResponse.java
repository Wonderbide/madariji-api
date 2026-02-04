package com.backcover.dto.gemini;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * DTO representing the structured response from Gemini.
 * This matches the JSON Schema defined in BookStructureSchema.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StructuredBookResponse {

    private List<PageResponse> pages;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageResponse {
        private int pageNumber;
        private boolean keepPage;
        private List<BlockResponse> blocks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BlockResponse {
        private String blockType;
        private String blockText;

        public boolean isVerseBlock() {
            return blockType != null && blockType.startsWith("verse_");
        }
    }

    private static final String HEMISTICH_SEPARATOR = "|||";

    /**
     * Converts this response to the structured.json format used by the frontend.
     *
     * Output format:
     * [
     *   {
     *     "pageNumber": 1,
     *     "content": [
     *       {
     *         "blockType": "paragraph",
     *         "words": [
     *           {"id": "p1b0w0", "text": "word1"},
     *           {"id": "p1b0w1", "text": "word2"}
     *         ]
     *       }
     *     ]
     *   }
     * ]
     */
    public List<StructuredPage> toStructuredFormat() {
        List<StructuredPage> result = new ArrayList<>();

        if (pages == null) {
            return result;
        }

        for (PageResponse page : pages) {
            // Skip pages marked as not useful
            if (!page.isKeepPage()) {
                continue;
            }

            StructuredPage structuredPage = new StructuredPage();
            structuredPage.setPageNumber(page.getPageNumber());
            structuredPage.setContent(new ArrayList<>());

            if (page.getBlocks() != null) {
                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    BlockResponse block = page.getBlocks().get(blockIndex);
                    StructuredBlock structuredBlock = new StructuredBlock();
                    structuredBlock.setBlockType(block.getBlockType());

                    String text = block.getBlockText();
                    // Pour les vers: detecter et splitter par |||
                    if (block.isVerseBlock() && text != null && text.contains(HEMISTICH_SEPARATOR)) {
                        String[] parts = text.split("\\|\\|\\|", 2);
                        String sadrText = parts[0].trim();
                        String ajzText = parts.length > 1 ? parts[1].trim() : "";
                        structuredBlock.setSadr(createWords(sadrText, page.getPageNumber(), blockIndex, "s"));
                        structuredBlock.setAjz(createWords(ajzText, page.getPageNumber(), blockIndex, "a"));
                    } else {
                        structuredBlock.setWords(createWords(text, page.getPageNumber(), blockIndex, "w"));
                    }
                    structuredPage.getContent().add(structuredBlock);
                }
            }

            result.add(structuredPage);
        }

        return result;
    }

    private List<StructuredWord> createWords(String text, int pageNum, int blockNum, String suffix) {
        List<StructuredWord> words = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return words;
        }

        // Split by whitespace while preserving Arabic text
        String[] parts = text.split("\\s+");

        for (int wordIndex = 0; wordIndex < parts.length; wordIndex++) {
            String word = parts[wordIndex].trim();
            if (!word.isEmpty()) {
                StructuredWord structuredWord = new StructuredWord();
                // Format: p{page}b{block}{suffix}{index} - ex: p1b0s0 (sadr), p1b0a0 (ajz), p1b0w0 (normal)
                structuredWord.setId(String.format("p%db%d%s%d", pageNum, blockNum, suffix, wordIndex));
                structuredWord.setText(word);
                words.add(structuredWord);
            }
        }

        return words;
    }

    // Inner classes for structured output format

    @Data
    public static class StructuredPage {
        private int pageNumber;
        private List<StructuredBlock> content;
    }

    @Data
    public static class StructuredBlock {
        private String blockType;
        private List<StructuredWord> words;
        // Pour les vers: hemistiches separes
        private List<StructuredWord> sadr;
        private List<StructuredWord> ajz;
    }

    @Data
    public static class StructuredWord {
        private String id;
        private String text;
    }
}
