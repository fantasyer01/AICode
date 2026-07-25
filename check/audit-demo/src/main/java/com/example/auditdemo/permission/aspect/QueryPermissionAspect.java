package com.example.auditdemo.permission.aspect;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.permission.annotation.QueryPermission;
import com.example.auditdemo.permission.service.FieldPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * AOP Aspect for filtering query results based on field permissions.
 * Intercepts methods annotated with @QueryPermission and filters out
 * fields that should be hidden.
 */
@Aspect
@Component
@Slf4j
@Order(100) // Run after other aspects
public class QueryPermissionAspect {

    @Autowired
    private FieldPermissionService fieldPermissionService;

    @Around("@annotation(com.example.auditdemo.permission.annotation.QueryPermission)")
    public Object filterQueryResult(ProceedingJoinPoint joinPoint) throws Throwable {
        // Execute the original method
        Object result = joinPoint.proceed();
        
        if (result == null) {
            return null;
        }

        // Get annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        QueryPermission annotation = method.getAnnotation(QueryPermission.class);

        // Build table mapping
        Map<Class<?>, String> tableMapping = buildTableMapping(annotation);
        
        if (tableMapping.isEmpty()) {
            log.warn("No table mapping found for @QueryPermission on method: {}", method.getName());
            return result;
        }

        // Filter the result
        filterResult(result, tableMapping);

        return result;
    }

    private Map<Class<?>, String> buildTableMapping(QueryPermission annotation) {
        Map<Class<?>, String> mapping = new HashMap<>();

        // Handle nestedTables
        String[] nestedTables = annotation.nestedTables();
        if (nestedTables != null && nestedTables.length > 0) {
            for (String entry : nestedTables) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String className = parts[0].trim();
                    String tableName = parts[1].trim();
                    Class<?> clazz = resolveClass(className);
                    if (clazz != null) {
                        mapping.put(clazz, tableName);
                    }
                }
            }
        }

        // Handle simple table (for backward compatibility)
        String table = annotation.table();
        if (StringUtils.hasText(table) && mapping.isEmpty()) {
            // For simple case, we'll detect the class from the result
            // This will be handled in filterResult method
            mapping.put(Void.class, table); // Placeholder
        }

        return mapping;
    }

    private Class<?> resolveClass(String className) {
        // Try common package prefixes
        String[] packagePrefixes = {
            "com.example.auditdemo.business.user.entity.",
            "com.example.auditdemo.business.order.entity.",
            "com.example.auditdemo.business.",
            ""
        };

        for (String prefix : packagePrefixes) {
            try {
                return Class.forName(prefix + className);
            } catch (ClassNotFoundException e) {
                // Try next prefix
            }
        }

        log.warn("Could not resolve class: {}", className);
        return null;
    }

    private void filterResult(Object result, Map<Class<?>, String> tableMapping) {
        if (result == null) {
            return;
        }

        // Handle Page<T>
        if (result instanceof Page) {
            Page<?> page = (Page<?>) result;
            List<?> records = page.getRecords();
            if (records != null) {
                for (Object record : records) {
                    filterSingleObject(record, tableMapping);
                }
            }
            return;
        }

        // Handle Collection
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            for (Object item : collection) {
                filterSingleObject(item, tableMapping);
            }
            return;
        }

        // Handle single object
        filterSingleObject(result, tableMapping);
    }

    private void filterSingleObject(Object obj, Map<Class<?>, String> tableMapping) {
        if (obj == null) {
            return;
        }

        // Check for placeholder (simple table case)
        if (tableMapping.containsKey(Void.class)) {
            String tableName = tableMapping.get(Void.class);
            fieldPermissionService.filterObject(obj, tableName);
            return;
        }

        // Use nested filtering
        fieldPermissionService.filterObjectWithNested(obj, tableMapping);
    }
}
