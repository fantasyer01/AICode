package com.example.auditdemo.business.order.entity;

import lombok.Data;

import java.util.List;

/**
 * Order DTO with items for form submission
 */
@Data
public class OrderDTO {

    /**
     * Order main info
     */
    private BizOrder order;

    /**
     * Order items
     */
    private List<BizOrderItem> items;
}
