package com.example.auditdemo.business.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.audit.annotation.AuditRequired;
import com.example.auditdemo.permission.annotation.QueryPermission;
import com.example.auditdemo.business.order.entity.BizOrder;
import com.example.auditdemo.business.order.entity.BizOrderItem;
import com.example.auditdemo.business.order.entity.OrderDTO;
import com.example.auditdemo.business.order.mapper.BizOrderItemMapper;
import com.example.auditdemo.business.order.mapper.BizOrderMapper;
import com.example.auditdemo.business.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Order service implementation demonstrating multi-table audit.
 * 
 * Note: The @AuditRequired annotation will intercept the method.
 * The actual database operations will only execute after audit approval.
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private BizOrderMapper orderMapper;

    @Autowired
    private BizOrderItemMapper orderItemMapper;

    @Override
    @QueryPermission(table = "biz_order")
    public Page<BizOrder> getList(int page, int size, String keyword) {
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BizOrder::getOrderNo, keyword)
                    .or().like(BizOrder::getCustomerName, keyword);
        }
        wrapper.orderByDesc(BizOrder::getId);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @QueryPermission(table = "biz_order")
    public BizOrder getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    @QueryPermission(table = "biz_order_item")
    public List<BizOrderItem> getItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<BizOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @QueryPermission(nestedTables = {"BizOrder:biz_order", "BizOrderItem:biz_order_item"})
    public OrderDTO getOrderWithItems(Long orderId) {
        OrderDTO dto = new OrderDTO();
        dto.setOrder(getById(orderId));
        dto.setItems(getItemsByOrderId(orderId));
        return dto;
    }

    /**
     * Create order with items - with audit review.
     * All operations will only execute after audit approval.
     */
    @Override
    @Transactional
    @AuditRequired(businessType = "ORDER_MGMT", desc = "Create Order")
    public void createOrder(OrderDTO orderDTO) {
        // Normal business logic - executes AFTER audit approval
        BizOrder order = orderDTO.getOrder();
        List<BizOrderItem> items = orderDTO.getItems();

        // Generate order number
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }

        // Set timestamps
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // Insert order
        orderMapper.insert(order);
        log.info("Order inserted: {}", order.getOrderNo());

        // Insert order items
        for (BizOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        log.info("Order items inserted: {} items", items.size());
    }

    /**
     * Update order with items - with audit review.
     * All operations will only execute after audit approval.
     */
    @Override
    @Transactional
    @AuditRequired(businessType = "ORDER_MGMT", desc = "Update Order")
    public void updateOrder(OrderDTO orderDTO) {
        // Normal business logic - executes AFTER audit approval
        BizOrder order = orderDTO.getOrder();
        List<BizOrderItem> newItems = orderDTO.getItems();

        // Update order
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Order updated: {}", order.getOrderNo());

        // Delete old items
        LambdaQueryWrapper<BizOrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BizOrderItem::getOrderId, order.getId());
        orderItemMapper.delete(deleteWrapper);

        // Insert new items
        for (BizOrderItem item : newItems) {
            item.setId(null); // Clear ID for insert
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        log.info("Order items updated: {} items", newItems.size());
    }

    /**
     * Delete order with items - with audit review.
     * All operations will only execute after audit approval.
     */
    @Override
    @Transactional
    @AuditRequired(businessType = "ORDER_MGMT", desc = "Delete Order")
    public void deleteOrder(Long orderId) {
        // Normal business logic - executes AFTER audit approval
        
        // Delete items first
        LambdaQueryWrapper<BizOrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BizOrderItem::getOrderId, orderId);
        orderItemMapper.delete(deleteWrapper);
        log.info("Order items deleted for orderId: {}", orderId);

        // Delete order
        orderMapper.deleteById(orderId);
        log.info("Order deleted: id={}", orderId);
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
