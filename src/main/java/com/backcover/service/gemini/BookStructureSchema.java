package com.backcover.service.gemini;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Defines the JSON Schema for Gemini structured output.
 * This schema ensures Gemini returns properly formatted book structure data.
 */
@Component
public class BookStructureSchema {

    /**
     * Returns the complete JSON Schema for book structure extraction.
     */
    public Map<String, Object> getSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "pages", Map.of(
                "type", "array",
                "description", "List of all pages in the document",
                "items", getPageSchema()
            )
        ));
        schema.put("required", List.of("pages"));
        return schema;
    }

    private Map<String, Object> getPageSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "pageNumber", Map.of(
                "type", "integer",
                "description", "Page number (starting from 1)"
            ),
            "keepPage", Map.of(
                "type", "boolean",
                "description", "true if page contains useful text content, false for empty/decorative pages"
            ),
            "blocks", Map.of(
                "type", "array",
                "description", "Text blocks on this page",
                "items", getBlockSchema()
            )
        ));
        schema.put("required", List.of("pageNumber", "keepPage", "blocks"));
        return schema;
    }

    private Map<String, Object> getBlockSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "blockType", Map.of(
                "type", "string",
                "enum", List.of(
                    "paragraph",
                    "title",
                    "subtitle",
                    "header",
                    "footer",
                    "quote",
                    "list_item",
                    "page_number",
                    "caption",
                    "verse_primary",
                    "verse_citation",
                    "verse_footnote"
                ),
                "description", "Semantic type of the text block"
            ),
            "blockText", Map.of(
                "type", "string",
                "description", "The text content of this block with all diacritics preserved"
            )
        ));
        schema.put("required", List.of("blockType", "blockText"));
        return schema;
    }

    /**
     * Returns the enrichment prompt for Arabic book analysis with tashkeel addition.
     * Uses default (no context) for backward compatibility.
     */
    public String getEnrichmentPrompt() {
        return getEnrichmentPrompt(null, null, null, null);
    }

    /**
     * Returns the enrichment prompt with book context for better tashkeel quality.
     *
     * @param title Book title (Arabic)
     * @param author Book author
     * @param genre Book genre
     * @param description Book description
     * @return The enrichment prompt with context
     */
    public String getEnrichmentPrompt(String title, String author, String genre, String description) {
        String contextBlock = "";
        if (title != null || author != null || genre != null || description != null) {
            contextBlock = String.format("""

            معلومات الكتاب (للسياق):
            - العنوان: %s
            - المؤلف: %s
            - النوع: %s
            - الوصف: %s

            استخدم هذه المعلومات لتحديد السياق اللغوي والمجال المعجمي المناسب للتشكيل.
            """,
                title != null ? title : "غير معروف",
                author != null ? author : "غير معروف",
                genre != null ? genre : "غير محدد",
                description != null ? description : "غير متوفر"
            );
        }

        return """
            أنت خبير في تحليل الوثائق واستخراج النصوص العربية وإضافة التشكيل.
            """ + contextBlock + """

            المهمة:
            حلل مستند PDF هذا واستخرج محتواه بشكل منظم مع إضافة التشكيل الكامل.

            التعليمات:
            1. لكل صفحة في المستند:
               - حدد ما إذا كانت الصفحة تحتوي على محتوى نصي مفيد (keepPage: true/false)
               - تجاهل الصفحات الفارغة أو صفحات الغلاف أو الصفحات الزخرفية البحتة

            2. لكل صفحة تحتوي على محتوى:
               - حدد كتل النص (فقرات، عناوين، عناوين فرعية، إلخ)
               - أضف التشكيل الكامل (الفتحة، الضمة، الكسرة، السكون، الشدة، التنوين) لجميع الكلمات
               - صنف كل كتلة حسب نوعها الدلالي

            3. أنواع الكتل الممكنة:
               - "paragraph": نص عادي
               - "title": عنوان رئيسي
               - "subtitle": عنوان فرعي
               - "header": ترويسة الصفحة
               - "footer": تذييل الصفحة
               - "quote": اقتباس نثري
               - "list_item": عنصر قائمة
               - "page_number": رقم الصفحة
               - "caption": تعليق على صورة
               - "verse_primary": أبيات شعرية هي موضوع الشرح (المتن الشعري الرئيسي)
               - "verse_citation": أبيات شعرية مستشهد بها للتوضيح أو كدليل نحوي (شاهد)
               - "verse_footnote": أبيات شعرية في الحاشية أو التعليقات الثانوية

            4. تعليمات خاصة بالشعر العربي:
               - إذا كان الكتاب شرحاً لقصيدة، فالأبيات المشروحة هي "verse_primary"
               - الأبيات المستشهد بها في سياق الشرح هي "verse_citation"
               - الأبيات في الهوامش أو الحواشي هي "verse_footnote"
               - مهم جداً: في كل بيت شعري، افصل بين الصدر والعجز بالرمز ||| (ثلاث شرطات عمودية)
               - مثال: "وَمَا ذَكَرْتُهُ مِنِ اقْتِبَاسٍ يَأْتِي ||| وَقَدْ أُحِيطَ بِالأَقْوَاسِ"

            مهم جداً:
            - أضف التشكيل الكامل لكل كلمة عربية
            - احفظ ترتيب القراءة الطبيعي (من اليمين إلى اليسار)
            - إذا كان النص يحتوي بالفعل على تشكيل، تأكد من صحته وأكمله إن لزم الأمر
            """;
    }
}
