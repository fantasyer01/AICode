package com.example.auditdemo.business.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Order item (detail) table entity
 */
@Data
@TableName("biz_order_item")
public class BizOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Order ID (foreign key)
     */
    private Long orderId;

    /**
     * Product name
     */
    private String productName;

    /**
     * Product specification
     */
    private String specification;

    /**
     * Unit price
     */
    private BigDecimal unitPrice;

    /**
     * Quantity
     */
    private Integer quantity;

    /**
     * Subtotal (unit_price * quantity)
     */
    private BigDecimal subtotal;
}
