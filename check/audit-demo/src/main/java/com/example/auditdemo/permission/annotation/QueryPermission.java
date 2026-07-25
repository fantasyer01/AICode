package com.example.auditdemo.permission.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should have field-level permission filtering applied.
 * The AOP aspect will intercept methods with this annotation and filter out
 * fields that the user doesn't have permission to query.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryPermission {

    /**
     * The primary table name for simple cases (single table query).
     * Use this when the return type maps directly to one table.
     * Example: "biz_user" for BizUser entity
     */
    String table() default "";

    /**
     * Table mappings for complex cases with nested objects.
     * Format: "ClassName:table_name" pairs.
     * Example: {"BizOrder:biz_order", "BizOrderItem:biz_order_item"}
     */
    String[] nestedTables() default {};
}
