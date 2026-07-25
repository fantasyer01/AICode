package com.example.auditdemo.audit.aspect;

import com.example.auditdemo.audit.annotation.AuditRequired;
import com.example.auditdemo.audit.context.AuditExecutionContext;
import com.example.auditdemo.audit.entity.SysAuditRequest;
import com.example.auditdemo.audit.mapper.SysAuditRequestMapper;
import com.example.auditdemo.audit.entity.SysAuditHistory;
import com.example.auditdemo.audit.mapper.SysAuditHistoryMapper;
import com.example.auditdemo.audit.model.SnapshotData;
import com.example.auditdemo.audit.snapshot.SnapshotBuilder;
import com.example.auditdemo.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AOP Aspect that intercepts methods annotated with @AuditRequired.
 * 
 * Behavior:
 * - If in callback mode (after approval): execute the method normally
 * - If not in callback mode: block execution, save to audit table
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Autowired
    private SysAuditRequestMapper requestMapper;

    @Autowired
    private SysAuditHistoryMapper historyMapper;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private List<SnapshotBuilder> snapshotBuilders;

    @Around("@annotation(auditRequired)")
    public Object around(ProceedingJoinPoint pjp, AuditRequired auditRequired) throws Throwable {
        // If in callback mode (approval execution), allow method to proceed
        if (AuditExecutionContext.isCallbackMode()) {
            log.debug("Callback mode: executing method normally");
            return pjp.proceed();
        }

        // Otherwise, intercept and save to audit table
        log.info("Intercepting method for audit: {}.{}",
                pjp.getTarget().getClass().getSimpleName(),
                pjp.getSignature().getName());

        // Get method signature info
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();

        // Get bean name
        String beanName = getBeanName(pjp.getTarget());

        // Serialize parameter types
        List<String> paramTypeList = new ArrayList<>();
        for (Class<?> paramType : method.getParameterTypes()) {
            paramTypeList.add(paramType.getName());
        }
        String paramTypes = objectMapper.writeValueAsString(paramTypeList);

        // Serialize arguments
        String methodArgs = objectMapper.writeValueAsString(args);

        // Detect operation type
        String operationType = auditRequired.operationType();
        if (!StringUtils.hasText(operationType)) {
            operationType = detectOperationType(method.getName());
        }

        // Build snapshot for display using business-specific builder
        String snapshotData = buildSnapshotData(auditRequired.businessType(), operationType, args);

        // Create audit request
        SysAuditRequest request = new SysAuditRequest();
        request.setBusinessType(auditRequired.businessType());
        request.setBusinessDesc(auditRequired.desc());
        request.setOperationType(operationType);
        request.setBeanName(beanName);
        request.setMethodName(method.getName());
        request.setParamTypes(paramTypes);
        request.setMethodArgs(methodArgs);
        request.setSnapshotData(snapshotData);
        request.setStatus(0); // Pending
        request.setSubmitUserId(UserContext.getCurrentUserId());
        request.setSubmitUserName(UserContext.getCurrentUserName());
        request.setSubmitTime(LocalDateTime.now());
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        requestMapper.insert(request);

        // Record history
        SysAuditHistory history = new SysAuditHistory();
        history.setRequestId(request.getId());
        history.setAction("SUBMIT");
        history.setOperatorId(UserContext.getCurrentUserId());
        history.setOperatorName(UserContext.getCurrentUserName());
        history.setOperateTime(LocalDateTime.now());
        historyMapper.insert(history);

        log.info("Audit request created: id={}, type={}, method={}.{}",
                request.getId(), auditRequired.businessType(), beanName, method.getName());

        // Return null or default value - method is NOT executed
        return getDefaultReturnValue(method.getReturnType());
    }

    /**
     * Get Spring bean name from target object
     */
    private String getBeanName(Object target) {
        Class<?> clazz = target.getClass();
        String className = clazz.getSimpleName();
        
        // Handle CGLIB proxy classes
        if (className.contains("$$")) {
            className = className.substring(0, className.indexOf("$$"));
        }
        
        // Convert to bean name (first letter lowercase)
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * Detect operation type from method name
     */
    private String detectOperationType(String methodName) {
        String lowerName = methodName.toLowerCase();
        if (lowerName.startsWith("add") || lowerName.startsWith("create") || lowerName.startsWith("insert") || lowerName.startsWith("save")) {
            return "INSERT";
        } else if (lowerName.startsWith("update") || lowerName.startsWith("modify") || lowerName.startsWith("edit")) {
            return "UPDATE";
        } else if (lowerName.startsWith("delete") || lowerName.startsWith("remove")) {
            return "DELETE";
        }
        return "UNKNOWN";
    }

    /**
     * Build snapshot data for display purposes using business-specific builders
     */
    private String buildSnapshotData(String businessType, String operationType, Object[] args) {
        try {
            // Find appropriate snapshot builder
            for (SnapshotBuilder builder : snapshotBuilders) {
                if (builder.supports(businessType)) {
                    SnapshotData snapshot = builder.buildSnapshot(operationType, args);
                    return objectMapper.writeValueAsString(snapshot);
                }
            }
            
            // Fallback to generic snapshot
            log.warn("No snapshot builder found for businessType: {}, using generic builder", businessType);
            return buildGenericSnapshot(args);
        } catch (Exception e) {
            log.error("Failed to build snapshot data", e);
            return buildGenericSnapshot(args);
        }
    }
    
    /**
     * Build generic snapshot as fallback
     */
    private String buildGenericSnapshot(Object[] args) {
        try {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("arguments", args);
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.warn("Failed to build generic snapshot", e);
            return "{}";
        }
    }

    /**
     * Get default return value for method return type
     */
    private Object getDefaultReturnValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        } else if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == double.class) return 0.0;
            if (returnType == float.class) return 0.0f;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == char.class) return '\0';
        }
        return null;
    }
}
