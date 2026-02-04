// Dans com/backcover/dto/WordAnalysisDto.java (ou un sous-package comme dto/analysis)
package com.backcover.dto; // Adaptez le package

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode; // Pour le champ 'details' si sa structure est variable

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut pas les champs null dans le JSON
public class WordAnalysisDto {
    private UUID id; // ID de l'analyse créée/sauvegardée - AJOUTÉ pour liaison avec word list
    private String word; // Le mot original (avec Tashkeel)
    private String type; // "اسم", "فعل", "حرف", "unknown"
    private String translation; // Traduction en français
    private String jidar; // Forme nue sans particules (ال, pronoms, etc.)
    private String root; // Racine trilitère avec espaces (ك ت ب)
    private String masdar; // Nom d'action (المصدر)
    private String wazn; // Schème morphologique (الوزن)
    private JsonNode details; // Peut contenir NounDetails, VerbDetails, ParticleDetails
    private String canonicalForm; // Forme canonique (nouveau champ ajouté par Gemini)
    private List<String> lexicalFields; // Champs lexicaux pour le regroupement sémantique (en arabe)
    private Map<String, List<String>> lexicalFieldsTranslated; // Champs lexicaux traduits par langue

    // Constructeurs, Getters, Setters

    public WordAnalysisDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
    public String getJidar() { return jidar; }
    public void setJidar(String jidar) { this.jidar = jidar; }
    public String getRoot() { return root; }
    public void setRoot(String root) { this.root = root; }
    public String getMasdar() { return masdar; }
    public void setMasdar(String masdar) { this.masdar = masdar; }
    public String getWazn() { return wazn; }
    public void setWazn(String wazn) { this.wazn = wazn; }
    public JsonNode getDetails() { return details; }
    public void setDetails(JsonNode details) { this.details = details; }
    public String getCanonicalForm() { return canonicalForm; }
    public void setCanonicalForm(String canonicalForm) { this.canonicalForm = canonicalForm; }
    public List<String> getLexicalFields() { return lexicalFields; }
    public void setLexicalFields(List<String> lexicalFields) { this.lexicalFields = lexicalFields; }
    public Map<String, List<String>> getLexicalFieldsTranslated() { return lexicalFieldsTranslated; }
    public void setLexicalFieldsTranslated(Map<String, List<String>> lexicalFieldsTranslated) { this.lexicalFieldsTranslated = lexicalFieldsTranslated; }

    // equals, hashCode, toString si nécessaire
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordAnalysisDto that = (WordAnalysisDto) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(type, that.type) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(jidar, that.jidar) &&
                Objects.equals(root, that.root) &&
                Objects.equals(masdar, that.masdar) &&
                Objects.equals(wazn, that.wazn) &&
                Objects.equals(details, that.details) &&
                Objects.equals(canonicalForm, that.canonicalForm) &&
                Objects.equals(lexicalFields, that.lexicalFields) &&
                Objects.equals(lexicalFieldsTranslated, that.lexicalFieldsTranslated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, type, translation, jidar, root, masdar, wazn, details, canonicalForm, lexicalFields, lexicalFieldsTranslated);
    }
}