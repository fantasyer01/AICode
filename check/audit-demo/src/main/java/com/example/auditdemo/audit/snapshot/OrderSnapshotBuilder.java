package com.example.auditdemo.audit.snapshot;

import com.example.auditdemo.audit.model.DisplayField;
import com.example.auditdemo.audit.model.SnapshotData;
import com.example.auditdemo.business.order.entity.BizOrder;
import com.example.auditdemo.business.order.entity.BizOrderItem;
import com.example.auditdemo.business.order.entity.OrderDTO;
import com.example.auditdemo.business.order.mapper.BizOrderItemMapper;
import com.example.auditdemo.business.order.mapper.BizOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot builder for Order Management (multi-table)
 */
@Component
public class OrderSnapshotBuilder implements SnapshotBuilder {
    
    @Autowired
    private BizOrderMapper orderMapper;
    
    @Autowired
    private BizOrderItemMapper orderItemMapper;
    
    @Override
    public boolean supports(String businessType) {
        return "ORDER_MGMT".equals(businessType);
    }
    
    @Override
    public SnapshotData buildSnapshot(String operationType, Object[] args) {
        if (args == null || args.length == 0) {
            return buildEmptySnapshot(operationType);
        }
        
        if ("INSERT".equals(operationType)) {
            return buildInsertSnapshot((OrderDTO) args[0]);
        } else if ("UPDATE".equals(operationType)) {
            return buildUpdateSnapshot((OrderDTO) args[0]);
        } else if ("DELETE".equals(operationType)) {
            return buildDeleteSnapshot((Long) args[0]);
        }
        
        return buildEmptySnapshot(operationType);
    }
    
    /**
     * Build snapshot for INSERT operation
     */
    private SnapshotData buildInsertSnapshot(OrderDTO orderDTO) {
        BizOrder order = orderDTO.getOrder();
        List<BizOrderItem> items = orderDTO.getItems();
        
        // Main order fields
        List<DisplayField> fields = new ArrayList<>();
        fields.add(new DisplayField("订单号", order.getOrderNo() != null ? order.getOrderNo() : "（待生成）"));
        fields.add(new DisplayField("客户名称", order.getCustomerName()));
        fields.add(new DisplayField("联系电话", order.getCustomerPhone() != null ? order.getCustomerPhone() : "-"));
        fields.add(new DisplayField("收货地址", order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
        fields.add(new DisplayField("订单状态", getStatusText(order.getStatus()), getStatusBadge(order.getStatus())));
        fields.add(new DisplayField("备注", order.getRemark() != null ? order.getRemark() : "-"));
        
        // Order items sub-section
        SnapshotData.SubSection itemsSection = buildItemsSection(items, null);
        
        return SnapshotData.builder()
                .businessType("ORDER_MGMT")
                .operationType("INSERT")
                .title("订单信息")
                .fields(fields)
                .subSections(Arrays.asList(itemsSection))
                .summary(String.format("新增订单: %s, 金额: ¥%s", 
                        order.getCustomerName(), 
                        order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0.00"))
                .build();
    }
    
    /**
     * Build snapshot for UPDATE operation with comparison
     */
    private SnapshotData buildUpdateSnapshot(OrderDTO newOrderDTO) {
        BizOrder newOrder = newOrderDTO.getOrder();
        List<BizOrderItem> newItems = newOrderDTO.getItems();
        
        // Fetch old data
        BizOrder oldOrder = orderMapper.selectById(newOrder.getId());
        List<BizOrderItem> oldItems = null;
        
        if (oldOrder == null) {
            // If old data not found, show as insert
            return buildInsertSnapshot(newOrderDTO);
        }
        
        // Fetch old items
        LambdaQueryWrapper<BizOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrderItem::getOrderId, oldOrder.getId());
        oldItems = orderItemMapper.selectList(wrapper);
        
        // Compare main order fields
        List<DisplayField> fields = new ArrayList<>();
        fields.add(createCompareField("订单号", oldOrder.getOrderNo(), newOrder.getOrderNo()));
        fields.add(createCompareField("客户名称", oldOrder.getCustomerName(), newOrder.getCustomerName()));
        fields.add(createCompareField("联系电话", oldOrder.getCustomerPhone(), newOrder.getCustomerPhone()));
        fields.add(createCompareField("收货地址", oldOrder.getShippingAddress(), newOrder.getShippingAddress()));
        
        // Status comparison
        DisplayField statusField = createCompareField("订单状态", 
                getStatusText(oldOrder.getStatus()), 
                getStatusText(newOrder.getStatus()));
        statusField.setBadge(getStatusBadge(newOrder.getStatus()));
        fields.add(statusField);
        
        fields.add(createCompareField("备注", oldOrder.getRemark(), newOrder.getRemark()));
        
        // Order items comparison
        SnapshotData.SubSection itemsSection = buildItemsSection(newItems, oldItems);
        
        return SnapshotData.builder()
                .businessType("ORDER_MGMT")
                .operationType("UPDATE")
                .title("订单信息")
                .fields(fields)
                .subSections(Arrays.asList(itemsSection))
                .summary(String.format("修改订单: %s, 金额: ¥%s", 
                        newOrder.getCustomerName(), 
                        newOrder.getTotalAmount() != null ? newOrder.getTotalAmount().toString() : "0.00"))
                .build();
    }
    
    /**
     * Build snapshot for DELETE operation
     */
    private SnapshotData buildDeleteSnapshot(Long orderId) {
        BizOrder order = orderMapper.selectById(orderId);
        
        if (order == null) {
            return SnapshotData.builder()
                    .businessType("ORDER_MGMT")
                    .operationType("DELETE")
                    .title("订单信息")
                    .summary("订单不存在 (ID: " + orderId + ")")
                    .build();
        }
        
        // Fetch order items
        LambdaQueryWrapper<BizOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrderItem::getOrderId, orderId);
        List<BizOrderItem> items = orderItemMapper.selectList(wrapper);
        
        // Main order fields
        List<DisplayField> fields = new ArrayList<>();
        fields.add(new DisplayField("订单ID", String.valueOf(order.getId())));
        fields.add(new DisplayField("订单号", order.getOrderNo()));
        fields.add(new DisplayField("客户名称", order.getCustomerName()));
        fields.add(new DisplayField("联系电话", order.getCustomerPhone() != null ? order.getCustomerPhone() : "-"));
        fields.add(new DisplayField("收货地址", order.getShippingAddress() != null ? order.getShippingAddress() : "-"));
        fields.add(new DisplayField("订单状态", getStatusText(order.getStatus()), getStatusBadge(order.getStatus())));
        fields.add(new DisplayField("备注", order.getRemark() != null ? order.getRemark() : "-"));
        
        // Order items
        SnapshotData.SubSection itemsSection = buildItemsSection(items, null);
        
        return SnapshotData.builder()
                .businessType("ORDER_MGMT")
                .operationType("DELETE")
                .title("订单信息")
                .fields(fields)
                .subSections(Arrays.asList(itemsSection))
                .summary(String.format("删除订单: %s (订单号: %s)", order.getCustomerName(), order.getOrderNo()))
                .build();
    }
    
    /**
     * Build items sub-section with optional comparison
     */
    private SnapshotData.SubSection buildItemsSection(List<BizOrderItem> newItems, List<BizOrderItem> oldItems) {
        List<String> headers = Arrays.asList("商品名称", "规格", "单价", "数量", "小计");
        
        // Build new items rows
        List<List<String>> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (BizOrderItem item : newItems) {
            List<String> row = new ArrayList<>();
            row.add(item.getProductName());
            row.add(item.getSpecification() != null ? item.getSpecification() : "-");
            row.add("¥" + (item.getUnitPrice() != null ? item.getUnitPrice().toString() : "0.00"));
            row.add(String.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
            row.add("¥" + (item.getSubtotal() != null ? item.getSubtotal().toString() : "0.00"));
            rows.add(row);
            
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }
        
        // Build old items rows if provided (for UPDATE comparison)
        List<List<String>> oldRows = null;
        if (oldItems != null && !oldItems.isEmpty()) {
            oldRows = new ArrayList<>();
            for (BizOrderItem item : oldItems) {
                List<String> row = new ArrayList<>();
                row.add(item.getProductName());
                row.add(item.getSpecification() != null ? item.getSpecification() : "-");
                row.add("¥" + (item.getUnitPrice() != null ? item.getUnitPrice().toString() : "0.00"));
                row.add(String.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
                row.add("¥" + (item.getSubtotal() != null ? item.getSubtotal().toString() : "0.00"));
                oldRows.add(row);
            }
        }
        
        return SnapshotData.SubSection.builder()
                .title("订单明细")
                .headers(headers)
                .rows(rows)
                .oldRows(oldRows)
                .footer("总金额: ¥" + total.toString())
                .build();
    }
    
    /**
     * Create a display field with comparison
     */
    private DisplayField createCompareField(String label, String oldValue, String newValue) {
        oldValue = oldValue != null ? oldValue : "-";
        newValue = newValue != null ? newValue : "-";
        
        DisplayField field = new DisplayField();
        field.setLabel(label);
        field.setValue(newValue);
        field.setOldValue(oldValue);
        field.setChanged(!Objects.equals(oldValue, newValue));
        
        return field;
    }
    
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
    
    private String getStatusBadge(Integer status) {
        if (status == null) return "secondary";
        switch (status) {
            case 0: return "warning";
            case 1: return "info";
            case 2: return "primary";
            case 3: return "success";
            case 4: return "secondary";
            default: return "secondary";
        }
    }
    
    private SnapshotData buildEmptySnapshot(String operationType) {
        return SnapshotData.builder()
                .businessType("ORDER_MGMT")
                .operationType(operationType)
                .title("订单信息")
                .summary("无数据")
                .build();
    }
}
