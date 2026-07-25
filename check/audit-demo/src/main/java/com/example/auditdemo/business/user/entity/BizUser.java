package com.example.auditdemo.business.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User entity for single table demo
 */
@Data
@TableName("biz_user")
public class BizUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * User name
     */
    private String username;

    /**
     * Real name
     */
    private String realName;

    /**
     * Email
     */
    private String email;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Department
     */
    private String department;

    /**
     * Status: 0-disabled, 1-enabled
     */
    private Integer status;

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
