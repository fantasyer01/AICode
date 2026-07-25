package com.example.auditdemo.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for audit request table
 */
@Data
@TableName("sys_audit_request")
public class SysAuditRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Business type identifier
     */
    private String businessType;

    /**
     * Business description
     */
    private String businessDesc;

    /**
     * Operation type: INSERT, UPDATE, DELETE
     */
    private String operationType;

    /**
     * Spring Bean name for callback
     */
    private String beanName;

    /**
     * Method name for callback
     */
    private String methodName;

    /**
     * Method parameter types (JSON array of class names)
     */
    private String paramTypes;

    /**
     * Method arguments (JSON serialized)
     */
    private String methodArgs;

    /**
     * Snapshot data for display (before/after data comparison)
     */
    private String snapshotData;

    /**
     * Status: 0-pending, 1-approved, 2-rejected
     */
    private Integer status;

    /**
     * Submitter user ID
     */
    private Long submitUserId;

    /**
     * Submitter user name
     */
    private String submitUserName;

    /**
     * Submit time
     */
    private LocalDateTime submitTime;

    /**
     * Submit remark
     */
    private String submitRemark;

    /**
     * Auditor user ID
     */
    private Long auditUserId;

    /**
     * Auditor user name
     */
    private String auditUserName;

    /**
     * Audit time
     */
    private LocalDateTime auditTime;

    /**
     * Audit remark
     */
    private String auditRemark;

    /**
     * Create time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Update time
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
