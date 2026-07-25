package com.example.auditdemo.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable audit review for a service method.
 * When this annotation is present, the method will be intercepted:
 * - Method execution is blocked
 * - Method call info is saved to audit table
 * - After approval, the method is called back automatically
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditRequired {

    /**
     * Business type identifier for categorizing audit requests
     */
    String businessType();

    /**
     * Business description for audit display
     */
    String desc() default "";

    /**
     * Operation type (optional, for display purposes)
     * Will be auto-detected if not specified
     */
    String operationType() default "";
}
