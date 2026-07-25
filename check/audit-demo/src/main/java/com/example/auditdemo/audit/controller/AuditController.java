package com.example.auditdemo.audit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.audit.entity.SysAuditHistory;
import com.example.auditdemo.audit.entity.SysAuditRequest;
import com.example.auditdemo.audit.service.AuditService;
import com.example.auditdemo.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Audit management controller
 */
@Controller
@RequestMapping("/audit")
@Slf4j
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Audit list page
     */
    @GetMapping
    public String listPage(Model model,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String businessType,
                           @RequestParam(required = false) Integer status) {
        Page<SysAuditRequest> pageResult = auditService.getList(page, size, businessType, status);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("businessType", businessType);
        model.addAttribute("status", status);
        return "audit/list";
    }

    /**
     * Pending audit list page
     */
    @GetMapping("/pending")
    public String pendingPage(Model model,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String businessType) {
        Page<SysAuditRequest> pageResult = auditService.getPendingList(page, size, businessType);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("businessType", businessType);
        return "audit/pending";
    }

    /**
     * Audit detail page
     */
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        SysAuditRequest request = auditService.getById(id);
        List<SysAuditHistory> history = auditService.getHistory(id);
        
        // Format JSON for display (technical view, kept as backup)
        String formattedArgs = null;
        try {
            if (request.getMethodArgs() != null) {
                JsonNode argsNode = objectMapper.readTree(request.getMethodArgs());
                formattedArgs = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(argsNode);
            }
        } catch (Exception e) {
            log.error("Failed to parse method args", e);
            formattedArgs = request.getMethodArgs();
        }
        
        // Parse snapshot data for business-friendly view
        String snapshotJson = request.getSnapshotData();

        model.addAttribute("request", request);
        model.addAttribute("history", history);
        model.addAttribute("formattedArgs", formattedArgs);
        model.addAttribute("snapshotJson", snapshotJson);
        return "audit/detail";
    }

    /**
     * Get audit request by ID (API)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Result<SysAuditRequest> getById(@PathVariable Long id) {
        return Result.success(auditService.getById(id));
    }

    /**
     * Get audit history (API)
     */
    @GetMapping("/api/{id}/history")
    @ResponseBody
    public Result<List<SysAuditHistory>> getHistory(@PathVariable Long id) {
        return Result.success(auditService.getHistory(id));
    }

    /**
     * Approve audit request (API)
     */
    @PostMapping("/api/{id}/approve")
    @ResponseBody
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String remark = body.getOrDefault("remark", "");
            auditService.approve(id, remark);
            return Result.success("Approved and executed", null);
        } catch (Exception e) {
            log.error("Failed to approve audit request", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Reject audit request (API)
     */
    @PostMapping("/api/{id}/reject")
    @ResponseBody
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String remark = body.getOrDefault("remark", "");
            if (remark.isEmpty()) {
                return Result.error("Please provide rejection reason");
            }
            auditService.reject(id, remark);
            return Result.success("Rejected", null);
        } catch (Exception e) {
            log.error("Failed to reject audit request", e);
            return Result.error(e.getMessage());
        }
    }
}
