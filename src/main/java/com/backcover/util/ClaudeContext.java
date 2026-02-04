package com.backcover.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide contextual information to Claude Code
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClaudeContext {
    
    /**
     * Contextual information about the code
     */
    String value();
    
    /**
     * Expected input/output format
     */
    String format() default "";
    
    /**
     * Valid values or ranges
     */
    String[] validValues() default {};
    
    /**
     * Example usage
     */
    String example() default "";
}