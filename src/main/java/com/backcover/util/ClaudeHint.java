package com.backcover.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide hints to Claude Code for better autonomous development
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClaudeHint {
    
    /**
     * Description of what this code does and why it exists
     */
    String value();
    
    /**
     * Related test files that should be updated when this code changes
     */
    String[] relatedTests() default {};
    
    /**
     * Database constraints or business rules to be aware of
     */
    String[] constraints() default {};
    
    /**
     * Performance considerations
     */
    String[] performance() default {};
    
    /**
     * Common pitfalls or gotchas
     */
    String[] warnings() default {};
    
    /**
     * Dependencies that might affect this code
     */
    String[] dependencies() default {};
    
    /**
     * Suggested test scenarios
     */
    String[] testScenarios() default {};
}