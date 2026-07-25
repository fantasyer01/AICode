package com.example.auditdemo.business.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.business.order.entity.BizOrder;
import com.example.auditdemo.business.order.entity.BizOrderItem;
import com.example.auditdemo.business.order.entity.OrderDTO;
import com.example.auditdemo.business.order.service.OrderService;
import com.example.auditdemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order management controller
 */
@Controller
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Order list page
     */
    @GetMapping
    public String listPage(Model model,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword) {
        Page<BizOrder> pageResult = orderService.getList(page, size, keyword);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        return "order/list";
    }

    /**
     * Add order page
     */
    @GetMapping("/add")
    public String addPage() {
        return "order/form";
    }

    /**
     * Edit order page
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        OrderDTO orderDTO = orderService.getOrderWithItems(id);
        model.addAttribute("order", orderDTO.getOrder());
        model.addAttribute("items", orderDTO.getItems());
        return "order/form";
    }

    /**
     * View order detail page
     */
    @GetMapping("/view/{id}")
    public String viewPage(@PathVariable Long id, Model model) {
        OrderDTO orderDTO = orderService.getOrderWithItems(id);
        model.addAttribute("order", orderDTO.getOrder());
        model.addAttribute("items", orderDTO.getItems());
        return "order/view";
    }

    /**
     * Get order by ID (API)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Result<OrderDTO> getById(@PathVariable Long id) {
        return Result.success(orderService.getOrderWithItems(id));
    }

    /**
     * Get order items (API)
     */
    @GetMapping("/api/{id}/items")
    @ResponseBody
    public Result<List<BizOrderItem>> getItems(@PathVariable Long id) {
        return Result.success(orderService.getItemsByOrderId(id));
    }

    /**
     * Create order (API) - triggers audit
     */
    @PostMapping("/api")
    @ResponseBody
    public Result<Void> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            orderService.createOrder(orderDTO);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to create order", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update order (API) - triggers audit
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public Result<Void> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        try {
            orderDTO.getOrder().setId(id);
            orderService.updateOrder(orderDTO);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to update order", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Delete order (API) - triggers audit
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Result<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to delete order", e);
            return Result.error(e.getMessage());
        }
    }
}
