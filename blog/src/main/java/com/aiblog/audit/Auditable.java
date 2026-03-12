package com.aiblog.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method for automatic audit logging.
 * The AOP aspect will record the operation in the audit_logs table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Operation type: INSERT, UPDATE, DELETE */
    String operation();

    /** Entity type name, e.g. "Article" */
    String entityType();
}
