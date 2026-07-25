package com.example.auditdemo.business.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.business.order.entity.BizOrder;
import com.example.auditdemo.business.order.entity.BizOrderItem;
import com.example.auditdemo.business.order.entity.OrderDTO;

import java.util.List;

/**
 * Order service interface
 */
public interface OrderService {

    /**
     * Get order list with pagination
     */
    Page<BizOrder> getList(int page, int size, String keyword);

    /**
     * Get order by ID
     */
    BizOrder getById(Long id);

    /**
     * Get order items by order ID
     */
    List<BizOrderItem> getItemsByOrderId(Long orderId);

    /**
     * Get order with items
     */
    OrderDTO getOrderWithItems(Long orderId);

    /**
     * Create order with items (with audit)
     */
    void createOrder(OrderDTO orderDTO);

    /**
     * Update order with items (with audit)
     */
    void updateOrder(OrderDTO orderDTO);

    /**
     * Delete order with items (with audit)
     */
    void deleteOrder(Long orderId);
}
