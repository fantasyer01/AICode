package com.example.auditdemo.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Field Permission entity for fine-grained query control
 */
@Data
@TableName("sys_field_permission")
public class SysFieldPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Table name: biz_user, biz_order, biz_order_item
     */
    private String tableName;

    /**
     * Field name in entity (camelCase)
     */
    private String fieldName;

    /**
     * Display label for admin UI
     */
    private String fieldLabel;

    /**
     * Queryable: 1=allowed, 0=hidden
     */
    private Integer queryable;

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
