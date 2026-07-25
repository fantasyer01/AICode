package com.example.auditdemo.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for audit history table
 */
@Data
@TableName("sys_audit_history")
public class SysAuditHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Reference to audit request
     */
    private Long requestId;

    /**
     * Action: SUBMIT, APPROVE, REJECT
     */
    private String action;

    /**
     * Operator user ID
     */
    private Long operatorId;

    /**
     * Operator user name
     */
    private String operatorName;

    /**
     * Operation time
     */
    private LocalDateTime operateTime;

    /**
     * Remark
     */
    private String remark;

    /**
     * Snapshot before operation (for audit trail)
     */
    private String snapshotBefore;
}
