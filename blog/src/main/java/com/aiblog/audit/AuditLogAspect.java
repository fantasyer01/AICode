package com.aiblog.audit;

import com.aiblog.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogService auditLogService;

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterAuditableMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String operation = auditable.operation();
            String entityType = auditable.entityType();
            Long entityId = extractEntityId(joinPoint, result);
            String operator = resolveOperator();

            if (entityId != null) {
                auditLogService.log(operation, entityType, entityId, operator);
            } else {
                log.warn("Audit: could not extract entityId for {} {} in method {}",
                        operation, entityType, joinPoint.getSignature().getName());
            }
        } catch (Exception e) {
            log.error("Audit logging failed for method {}: {}",
                    joinPoint.getSignature().getName(), e.getMessage(), e);
        }
    }

    /**
     * Extracts the entity ID from method arguments or return value.
     * For INSERT: reads id from the return value (e.g. ArticleResponse.getId())
     * For UPDATE/DELETE: reads the first Long argument
     */
    private Long extractEntityId(JoinPoint joinPoint, Object result) {
        // For UPDATE/DELETE, the first Long parameter is typically the entity ID
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        // For INSERT, try to get id from the return value via reflection
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (NoSuchMethodException e) {
                // Return type doesn't have getId(), that's ok
            } catch (Exception e) {
                log.debug("Could not extract id from return value: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Determines the operator based on the current HTTP request path.
     * /admin/** -> "admin"
     * /api/**   -> "api-key"
     * fallback  -> "system"
     */
    private String resolveOperator() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String path = request.getRequestURI();
                if (path.startsWith("/admin")) {
                    return "admin";
                }
                if (path.startsWith("/api")) {
                    return "api-key";
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve request context for operator: {}", e.getMessage());
        }
        return "system";
    }
}
