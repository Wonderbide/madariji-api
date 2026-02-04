package com.backcover.dto.wordlist;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordAnalysisDetailsDto {
    
    private String type;
    private String jidar; // Forme nue sans particules (ال, pronoms, etc.)
    private String root; // Racine trilitère avec espaces (ك ت ب)
    private String masdar; // Nom d'action (المصدر)
    private String wazn; // Schème morphologique (الوزن)
    private String canonicalForm;
    private String meaning;
    private String function;
    private String translation; // Traduction dans la langue cible
    private List<String> lexicalFields;
    private Map<String, List<String>> lexicalFieldsTranslated;
    private JsonNode additionalDetails; // Pour données flexibles supplémentaires

    // Constructors
    public WordAnalysisDetailsDto() {}

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJidar() {
        return jidar;
    }

    public void setJidar(String jidar) {
        this.jidar = jidar;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getMasdar() {
        return masdar;
    }

    public void setMasdar(String masdar) {
        this.masdar = masdar;
    }

    public String getWazn() {
        return wazn;
    }

    public void setWazn(String wazn) {
        this.wazn = wazn;
    }

    public String getCanonicalForm() {
        return canonicalForm;
    }

    public void setCanonicalForm(String canonicalForm) {
        this.canonicalForm = canonicalForm;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public List<String> getLexicalFields() {
        return lexicalFields;
    }

    public void setLexicalFields(List<String> lexicalFields) {
        this.lexicalFields = lexicalFields;
    }

    public Map<String, List<String>> getLexicalFieldsTranslated() {
        return lexicalFieldsTranslated;
    }

    public void setLexicalFieldsTranslated(Map<String, List<String>> lexicalFieldsTranslated) {
        this.lexicalFieldsTranslated = lexicalFieldsTranslated;
    }

    public JsonNode getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(JsonNode additionalDetails) {
        this.additionalDetails = additionalDetails;
    }
}