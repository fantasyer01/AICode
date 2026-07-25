package com.example.auditdemo.business.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order main table entity
 */
@Data
@TableName("biz_order")
public class BizOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Order number
     */
    private String orderNo;

    /**
     * Customer name
     */
    private String customerName;

    /**
     * Customer phone
     */
    private String customerPhone;

    /**
     * Shipping address
     */
    private String shippingAddress;

    /**
     * Total amount
     */
    private BigDecimal totalAmount;

    /**
     * Status: 0-pending, 1-confirmed, 2-shipped, 3-completed, 4-cancelled
     */
    private Integer status;

    /**
     * Remark
     */
    private String remark;

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
