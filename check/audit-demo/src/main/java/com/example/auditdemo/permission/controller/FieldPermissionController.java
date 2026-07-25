package com.example.auditdemo.permission.controller;

import com.example.auditdemo.common.Result;
import com.example.auditdemo.permission.entity.SysFieldPermission;
import com.example.auditdemo.permission.service.FieldPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Field Permission Controller for admin management
 */
@Controller
@RequestMapping("/permission")
@Slf4j
public class FieldPermissionController {

    @Autowired
    private FieldPermissionService fieldPermissionService;

    /**
     * Admin page for field permission management
     */
    @GetMapping
    public String permissionPage(Model model,
                                  @RequestParam(required = false) String tableName) {
        List<String> tableNames = fieldPermissionService.getAllTableNames();
        model.addAttribute("tableNames", tableNames);
        
        // Default to first table if not specified
        if (tableName == null && !tableNames.isEmpty()) {
            tableName = tableNames.get(0);
        }
        
        if (tableName != null) {
            List<SysFieldPermission> permissions = fieldPermissionService.getPermissionsByTable(tableName);
            model.addAttribute("permissions", permissions);
            model.addAttribute("selectedTable", tableName);
        }
        
        return "permission/list";
    }

    /**
     * API: Get all table names
     */
    @GetMapping("/api/tables")
    @ResponseBody
    public Result<List<String>> getTableNames() {
        return Result.success(fieldPermissionService.getAllTableNames());
    }

    /**
     * API: Get permissions for a table
     */
    @GetMapping("/api/table/{tableName}")
    @ResponseBody
    public Result<List<SysFieldPermission>> getPermissions(@PathVariable String tableName) {
        return Result.success(fieldPermissionService.getPermissionsByTable(tableName));
    }

    /**
     * API: Get queryable fields for a table (used by front-end)
     */
    @GetMapping("/api/queryable/{tableName}")
    @ResponseBody
    public Result<Set<String>> getQueryableFields(@PathVariable String tableName) {
        return Result.success(fieldPermissionService.getQueryableFields(tableName));
    }

    /**
     * API: Update single permission
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public Result<Void> updatePermission(@PathVariable Long id,
                                          @RequestBody SysFieldPermission permission) {
        try {
            permission.setId(id);
            fieldPermissionService.updatePermission(permission);
            return Result.success("Permission updated", null);
        } catch (Exception e) {
            log.error("Failed to update permission", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * API: Toggle queryable status for a field
     */
    @PostMapping("/api/toggle/{id}")
    @ResponseBody
    public Result<Void> toggleQueryable(@PathVariable Long id) {
        try {
            SysFieldPermission permission = fieldPermissionService.getById(id);
            if (permission == null) {
                return Result.error("Permission not found");
            }
            permission.setQueryable(permission.getQueryable() == 1 ? 0 : 1);
            fieldPermissionService.updatePermission(permission);
            return Result.success("Permission toggled", null);
        } catch (Exception e) {
            log.error("Failed to toggle permission", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * API: Batch update queryable status
     */
    @PostMapping("/api/batch")
    @ResponseBody
    public Result<Void> batchUpdateQueryable(@RequestBody Map<String, Object> request) {
        try {
            String tableName = (String) request.get("tableName");
            @SuppressWarnings("unchecked")
            List<String> fieldNames = (List<String>) request.get("fieldNames");
            Integer queryable = (Integer) request.get("queryable");
            
            fieldPermissionService.batchUpdateQueryable(tableName, fieldNames, queryable);
            return Result.success("Permissions updated", null);
        } catch (Exception e) {
            log.error("Failed to batch update permissions", e);
            return Result.error(e.getMessage());
        }
    }
}
