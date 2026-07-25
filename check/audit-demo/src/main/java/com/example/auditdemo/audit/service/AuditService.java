package com.example.auditdemo.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.audit.entity.SysAuditHistory;
import com.example.auditdemo.audit.entity.SysAuditRequest;
import com.example.auditdemo.audit.model.AuditData;

import java.util.List;

/**
 * Service interface for audit operations
 */
public interface AuditService {

    /**
     * Submit an audit request
     *
     * @param businessType Business type identifier
     * @param desc         Business description
     * @param data         Audit data containing table operations
     * @return Request ID
     */
    Long submitAudit(String businessType, String desc, AuditData data);

    /**
     * Approve an audit request and execute the operations
     *
     * @param requestId Request ID
     * @param remark    Approval remark
     */
    void approve(Long requestId, String remark);

    /**
     * Reject an audit request
     *
     * @param requestId Request ID
     * @param remark    Rejection remark
     */
    void reject(Long requestId, String remark);

    /**
     * Get pending audit requests with pagination
     *
     * @param page         Page number
     * @param size         Page size
     * @param businessType Optional business type filter
     * @return Page of audit requests
     */
    Page<SysAuditRequest> getPendingList(int page, int size, String businessType);

    /**
     * Get all audit requests with pagination
     *
     * @param page         Page number
     * @param size         Page size
     * @param businessType Optional business type filter
     * @param status       Optional status filter
     * @return Page of audit requests
     */
    Page<SysAuditRequest> getList(int page, int size, String businessType, Integer status);

    /**
     * Get audit request by ID
     *
     * @param requestId Request ID
     * @return Audit request
     */
    SysAuditRequest getById(Long requestId);

    /**
     * Get audit history for a request
     *
     * @param requestId Request ID
     * @return List of history records
     */
    List<SysAuditHistory> getHistory(Long requestId);
}
