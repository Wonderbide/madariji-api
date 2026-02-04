package com.backcover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LexicalFieldTranslationService {
    
    private static final Logger log = LoggerFactory.getLogger(LexicalFieldTranslationService.class);
    
    // Dictionnaire statique des traductions des champs lexicaux
    private static final Map<String, Map<String, String>> LEXICAL_FIELD_TRANSLATIONS = createTranslationMap();
    
    /**
     * Crée le dictionnaire des traductions
     */
    private static Map<String, Map<String, String>> createTranslationMap() {
        Map<String, Map<String, String>> translations = new HashMap<>();
        
        // Médecine et Santé
        translations.put("الطب", Map.of("fr", "Médecine", "en", "Medicine", "es", "Medicina", "de", "Medizin"));
        translations.put("الصحة", Map.of("fr", "Santé", "en", "Health", "es", "Salud", "de", "Gesundheit"));
        translations.put("المرض", Map.of("fr", "Maladie", "en", "Disease", "es", "Enfermedad", "de", "Krankheit"));
        translations.put("العلاج", Map.of("fr", "Traitement", "en", "Treatment", "es", "Tratamiento", "de", "Behandlung"));
        
        // Professions et Travail
        translations.put("المهن", Map.of("fr", "Professions", "en", "Professions", "es", "Profesiones", "de", "Berufe"));
        translations.put("العمل", Map.of("fr", "Travail", "en", "Work", "es", "Trabajo", "de", "Arbeit"));
        translations.put("الوظيفة", Map.of("fr", "Emploi", "en", "Job", "es", "Empleo", "de", "Beruf"));
        
        // Éducation
        translations.put("التعليم", Map.of("fr", "Éducation", "en", "Education", "es", "Educación", "de", "Bildung"));
        translations.put("الدراسة", Map.of("fr", "Études", "en", "Studies", "es", "Estudios", "de", "Studium"));
        translations.put("المدرسة", Map.of("fr", "École", "en", "School", "es", "Escuela", "de", "Schule"));
        translations.put("الجامعة", Map.of("fr", "Université", "en", "University", "es", "Universidad", "de", "Universität"));
        
        // Nourriture et Cuisine
        translations.put("الطعام", Map.of("fr", "Nourriture", "en", "Food", "es", "Comida", "de", "Essen"));
        translations.put("الطبخ", Map.of("fr", "Cuisine", "en", "Cooking", "es", "Cocina", "de", "Kochen"));
        translations.put("الشراب", Map.of("fr", "Boisson", "en", "Drink", "es", "Bebida", "de", "Getränk"));
        
        // Famille et Relations
        translations.put("الأسرة", Map.of("fr", "Famille", "en", "Family", "es", "Familia", "de", "Familie"));
        translations.put("العائلة", Map.of("fr", "Famille", "en", "Family", "es", "Familia", "de", "Familie"));
        translations.put("الزواج", Map.of("fr", "Mariage", "en", "Marriage", "es", "Matrimonio", "de", "Ehe"));
        
        // Émotions et Sentiments
        translations.put("العواطف", Map.of("fr", "Émotions", "en", "Emotions", "es", "Emociones", "de", "Emotionen"));
        translations.put("المشاعر", Map.of("fr", "Sentiments", "en", "Feelings", "es", "Sentimientos", "de", "Gefühle"));
        translations.put("الحب", Map.of("fr", "Amour", "en", "Love", "es", "Amor", "de", "Liebe"));
        translations.put("الحزن", Map.of("fr", "Tristesse", "en", "Sadness", "es", "Tristeza", "de", "Traurigkeit"));
        translations.put("الفرح", Map.of("fr", "Joie", "en", "Joy", "es", "Alegría", "de", "Freude"));
        
        // Religion et Spiritualité
        translations.put("الدين", Map.of("fr", "Religion", "en", "Religion", "es", "Religión", "de", "Religion"));
        translations.put("العبادة", Map.of("fr", "Culte", "en", "Worship", "es", "Adoración", "de", "Anbetung"));
        translations.put("الصلاة", Map.of("fr", "Prière", "en", "Prayer", "es", "Oración", "de", "Gebet"));
        
        // Nature et Environnement
        translations.put("الطبيعة", Map.of("fr", "Nature", "en", "Nature", "es", "Naturaleza", "de", "Natur"));
        translations.put("البيئة", Map.of("fr", "Environnement", "en", "Environment", "es", "Medio ambiente", "de", "Umwelt"));
        translations.put("الحيوانات", Map.of("fr", "Animaux", "en", "Animals", "es", "Animales", "de", "Tiere"));
        translations.put("النباتات", Map.of("fr", "Plantes", "en", "Plants", "es", "Plantas", "de", "Pflanzen"));
        
        // Temps et Espace
        translations.put("الوقت", Map.of("fr", "Temps", "en", "Time", "es", "Tiempo", "de", "Zeit"));
        translations.put("المكان", Map.of("fr", "Lieu", "en", "Place", "es", "Lugar", "de", "Ort"));
        translations.put("الجغرافيا", Map.of("fr", "Géographie", "en", "Geography", "es", "Geografía", "de", "Geographie"));
        
        // Arts et Culture
        translations.put("الفن", Map.of("fr", "Art", "en", "Art", "es", "Arte", "de", "Kunst"));
        translations.put("الثقافة", Map.of("fr", "Culture", "en", "Culture", "es", "Cultura", "de", "Kultur"));
        translations.put("الموسيقى", Map.of("fr", "Musique", "en", "Music", "es", "Música", "de", "Musik"));
        
        // Transport et Voyage
        translations.put("السفر", Map.of("fr", "Voyage", "en", "Travel", "es", "Viaje", "de", "Reise"));
        translations.put("النقل", Map.of("fr", "Transport", "en", "Transportation", "es", "Transporte", "de", "Transport"));
        translations.put("السياحة", Map.of("fr", "Tourisme", "en", "Tourism", "es", "Turismo", "de", "Tourismus"));
        
        // Commerce et Économie
        translations.put("التجارة", Map.of("fr", "Commerce", "en", "Trade", "es", "Comercio", "de", "Handel"));
        translations.put("الاقتصاد", Map.of("fr", "Économie", "en", "Economy", "es", "Economía", "de", "Wirtschaft"));
        translations.put("المال", Map.of("fr", "Argent", "en", "Money", "es", "Dinero", "de", "Geld"));
        
        // Technologie
        translations.put("التكنولوجيا", Map.of("fr", "Technologie", "en", "Technology", "es", "Tecnología", "de", "Technologie"));
        translations.put("الحاسوب", Map.of("fr", "Ordinateur", "en", "Computer", "es", "Computadora", "de", "Computer"));
        
        // Couleurs et Apparence
        translations.put("الألوان", Map.of("fr", "Couleurs", "en", "Colors", "es", "Colores", "de", "Farben"));
        translations.put("الشكل", Map.of("fr", "Forme", "en", "Shape", "es", "Forma", "de", "Form"));
        
        // Vêtements
        translations.put("الملابس", Map.of("fr", "Vêtements", "en", "Clothing", "es", "Ropa", "de", "Kleidung"));
        
        // Sports et Loisirs
        translations.put("الرياضة", Map.of("fr", "Sport", "en", "Sports", "es", "Deporte", "de", "Sport"));
        translations.put("الترفيه", Map.of("fr", "Divertissement", "en", "Entertainment", "es", "Entretenimiento", "de", "Unterhaltung"));
        
        // Sciences
        translations.put("العلوم", Map.of("fr", "Sciences", "en", "Sciences", "es", "Ciencias", "de", "Wissenschaften"));
        translations.put("الفيزياء", Map.of("fr", "Physique", "en", "Physics", "es", "Física", "de", "Physik"));
        translations.put("الكيمياء", Map.of("fr", "Chimie", "en", "Chemistry", "es", "Química", "de", "Chemie"));
        
        // Communication
        translations.put("التواصل", Map.of("fr", "Communication", "en", "Communication", "es", "Comunicación", "de", "Kommunikation"));
        translations.put("اللغة", Map.of("fr", "Langue", "en", "Language", "es", "Idioma", "de", "Sprache"));
        
        return translations;
    }
    
    /**
     * Traduit une liste de champs lexicaux arabes vers la langue cible
     */
    public List<String> translate(List<String> arabicFields, String targetLanguageCode) {
        if (arabicFields == null || arabicFields.isEmpty()) {
            return new ArrayList<>();
        }
        
        return arabicFields.stream()
                .map(field -> translateField(field, targetLanguageCode))
                .collect(Collectors.toList());
    }
    
    /**
     * Traduit les champs lexicaux vers toutes les langues supportées
     */
    public Map<String, List<String>> translateToAllLanguages(List<String> arabicFields) {
        if (arabicFields == null || arabicFields.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, List<String>> result = new HashMap<>();
        Set<String> supportedLanguages = getSupportedLanguages();
        
        for (String language : supportedLanguages) {
            List<String> translatedFields = translate(arabicFields, language);
            result.put(language, translatedFields);
        }
        
        return result;
    }
    
    /**
     * Traduit un champ lexical unique
     */
    private String translateField(String arabicField, String targetLanguageCode) {
        Map<String, String> translations = LEXICAL_FIELD_TRANSLATIONS.get(arabicField);
        
        if (translations != null && translations.containsKey(targetLanguageCode)) {
            String translation = translations.get(targetLanguageCode);
            log.debug("Translated '{}' to '{}' in language '{}'", arabicField, translation, targetLanguageCode);
            return translation;
        }
        
        // Fallback: retourner le terme arabe si pas de traduction
        log.debug("No translation found for '{}' in language '{}', keeping original", arabicField, targetLanguageCode);
        return arabicField;
    }
    
    /**
     * Retourne l'ensemble des langues supportées
     */
    public Set<String> getSupportedLanguages() {
        Set<String> languages = new HashSet<>();
        LEXICAL_FIELD_TRANSLATIONS.values().forEach(translations -> languages.addAll(translations.keySet()));
        return languages;
    }
    
    /**
     * Vérifie si un champ lexical arabe est supporté
     */
    public boolean isFieldSupported(String arabicField) {
        return LEXICAL_FIELD_TRANSLATIONS.containsKey(arabicField);
    }
    
    /**
     * Retourne toutes les traductions disponibles pour un champ
     */
    public Map<String, String> getAllTranslations(String arabicField) {
        return LEXICAL_FIELD_TRANSLATIONS.getOrDefault(arabicField, new HashMap<>());
    }
    
    /**
     * Statistiques sur la couverture des traductions
     */
    public Map<String, Object> getTranslationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFields", LEXICAL_FIELD_TRANSLATIONS.size());
        stats.put("supportedLanguages", getSupportedLanguages());
        stats.put("languageCount", getSupportedLanguages().size());
        return stats;
    }
}