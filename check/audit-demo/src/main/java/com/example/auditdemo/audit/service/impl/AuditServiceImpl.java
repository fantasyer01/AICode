package com.example.auditdemo.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.audit.context.AuditExecutionContext;
import com.example.auditdemo.audit.entity.SysAuditHistory;
import com.example.auditdemo.audit.entity.SysAuditRequest;
import com.example.auditdemo.audit.mapper.SysAuditHistoryMapper;
import com.example.auditdemo.audit.mapper.SysAuditRequestMapper;
import com.example.auditdemo.audit.service.AuditService;
import com.example.auditdemo.common.UserContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of AuditService
 */
@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

    /** Status: Pending */
    public static final int STATUS_PENDING = 0;
    /** Status: Approved */
    public static final int STATUS_APPROVED = 1;
    /** Status: Rejected */
    public static final int STATUS_REJECTED = 2;

    @Autowired
    private SysAuditRequestMapper requestMapper;

    @Autowired
    private SysAuditHistoryMapper historyMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Long submitAudit(String businessType, String desc, 
                            com.example.auditdemo.audit.model.AuditData data) {
        // This method is no longer used in the new implementation
        // Kept for interface compatibility
        throw new UnsupportedOperationException("Use @AuditRequired annotation instead");
    }

    @Override
    @Transactional
    public void approve(Long requestId, String remark) {
        SysAuditRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Audit request not found: " + requestId);
        }

        if (request.getStatus() != STATUS_PENDING) {
            throw new IllegalStateException("Audit request is not pending: " + requestId);
        }

        try {
            // Execute the original method via callback
            executeCallback(request);

            // Update request status
            request.setStatus(STATUS_APPROVED);
            request.setAuditUserId(UserContext.getCurrentUserId());
            request.setAuditUserName(UserContext.getCurrentUserName());
            request.setAuditTime(LocalDateTime.now());
            request.setAuditRemark(remark);
            request.setUpdateTime(LocalDateTime.now());

            requestMapper.updateById(request);

            // Record history
            saveHistory(requestId, "APPROVE", remark);

            log.info("Audit request approved and executed: id={}", requestId);

        } catch (Exception e) {
            log.error("Failed to execute callback for audit request: " + requestId, e);
            throw new RuntimeException("Failed to execute approved operation: " + e.getMessage(), e);
        }
    }

    /**
     * Execute the original method via reflection
     */
    private void executeCallback(SysAuditRequest request) throws Exception {
        String beanName = request.getBeanName();
        String methodName = request.getMethodName();
        String paramTypesJson = request.getParamTypes();
        String methodArgsJson = request.getMethodArgs();

        log.info("Executing callback: bean={}, method={}", beanName, methodName);

        // Get the Spring bean
        Object bean = applicationContext.getBean(beanName);
        
        // Parse parameter types
        List<String> paramTypeNames = objectMapper.readValue(paramTypesJson, 
                new TypeReference<List<String>>() {});
        
        Class<?>[] paramTypes = new Class<?>[paramTypeNames.size()];
        for (int i = 0; i < paramTypeNames.size(); i++) {
            paramTypes[i] = Class.forName(paramTypeNames.get(i));
        }

        // Get the method
        Method method = bean.getClass().getMethod(methodName, paramTypes);

        // Parse arguments
        JsonNode argsNode = objectMapper.readTree(methodArgsJson);
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = objectMapper.treeToValue(argsNode.get(i), paramTypes[i]);
        }

        // Enter callback mode to bypass AOP interception
        AuditExecutionContext.enterCallbackMode();
        try {
            // Invoke the method
            method.invoke(bean, args);
            log.info("Callback executed successfully");
        } finally {
            // Always exit callback mode
            AuditExecutionContext.exitCallbackMode();
        }
    }

    @Override
    @Transactional
    public void reject(Long requestId, String remark) {
        SysAuditRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Audit request not found: " + requestId);
        }

        if (request.getStatus() != STATUS_PENDING) {
            throw new IllegalStateException("Audit request is not pending: " + requestId);
        }

        // Update request status
        request.setStatus(STATUS_REJECTED);
        request.setAuditUserId(UserContext.getCurrentUserId());
        request.setAuditUserName(UserContext.getCurrentUserName());
        request.setAuditTime(LocalDateTime.now());
        request.setAuditRemark(remark);
        request.setUpdateTime(LocalDateTime.now());

        requestMapper.updateById(request);

        // Record history
        saveHistory(requestId, "REJECT", remark);

        log.info("Audit request rejected: id={}", requestId);
    }

    @Override
    public Page<SysAuditRequest> getPendingList(int page, int size, String businessType) {
        return getList(page, size, businessType, STATUS_PENDING);
    }

    @Override
    public Page<SysAuditRequest> getList(int page, int size, String businessType, Integer status) {
        LambdaQueryWrapper<SysAuditRequest> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(businessType)) {
            wrapper.eq(SysAuditRequest::getBusinessType, businessType);
        }
        if (status != null) {
            wrapper.eq(SysAuditRequest::getStatus, status);
        }
        
        wrapper.orderByDesc(SysAuditRequest::getCreateTime);

        return requestMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public SysAuditRequest getById(Long requestId) {
        return requestMapper.selectById(requestId);
    }

    @Override
    public List<SysAuditHistory> getHistory(Long requestId) {
        LambdaQueryWrapper<SysAuditHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditHistory::getRequestId, requestId);
        wrapper.orderByAsc(SysAuditHistory::getOperateTime);
        return historyMapper.selectList(wrapper);
    }

    private void saveHistory(Long requestId, String action, String remark) {
        SysAuditHistory history = new SysAuditHistory();
        history.setRequestId(requestId);
        history.setAction(action);
        history.setOperatorId(UserContext.getCurrentUserId());
        history.setOperatorName(UserContext.getCurrentUserName());
        history.setOperateTime(LocalDateTime.now());
        history.setRemark(remark);

        historyMapper.insert(history);
    }
}
