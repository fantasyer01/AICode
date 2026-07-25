package com.example.auditdemo.audit.snapshot;

import com.example.auditdemo.audit.model.DisplayField;
import com.example.auditdemo.audit.model.SnapshotData;
import com.example.auditdemo.business.user.entity.BizUser;
import com.example.auditdemo.business.user.mapper.BizUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Snapshot builder for User Management (single table)
 */
@Component
public class UserSnapshotBuilder implements SnapshotBuilder {
    
    @Autowired
    private BizUserMapper userMapper;
    
    @Override
    public boolean supports(String businessType) {
        return "USER_MGMT".equals(businessType);
    }
    
    @Override
    public SnapshotData buildSnapshot(String operationType, Object[] args) {
        if (args == null || args.length == 0) {
            return buildEmptySnapshot(operationType);
        }
        
        if ("INSERT".equals(operationType)) {
            return buildInsertSnapshot((BizUser) args[0]);
        } else if ("UPDATE".equals(operationType)) {
            return buildUpdateSnapshot((BizUser) args[0]);
        } else if ("DELETE".equals(operationType)) {
            return buildDeleteSnapshot((Long) args[0]);
        }
        
        return buildEmptySnapshot(operationType);
    }
    
    /**
     * Build snapshot for INSERT operation
     */
    private SnapshotData buildInsertSnapshot(BizUser user) {
        List<DisplayField> fields = new ArrayList<>();
        
        fields.add(new DisplayField("用户名", user.getUsername()));
        fields.add(new DisplayField("姓名", user.getRealName()));
        fields.add(new DisplayField("邮箱", user.getEmail() != null ? user.getEmail() : "-"));
        fields.add(new DisplayField("电话", user.getPhone() != null ? user.getPhone() : "-"));
        fields.add(new DisplayField("部门", user.getDepartment() != null ? user.getDepartment() : "-"));
        fields.add(new DisplayField("状态", 
                user.getStatus() == 1 ? "启用" : "禁用", 
                user.getStatus() == 1 ? "success" : "secondary"));
        
        return SnapshotData.builder()
                .businessType("USER_MGMT")
                .operationType("INSERT")
                .title("用户信息")
                .fields(fields)
                .summary(String.format("新增用户: %s (%s)", user.getRealName(), user.getUsername()))
                .build();
    }
    
    /**
     * Build snapshot for UPDATE operation with comparison
     */
    private SnapshotData buildUpdateSnapshot(BizUser newUser) {
        List<DisplayField> fields = new ArrayList<>();
        
        // Fetch old data from database
        BizUser oldUser = userMapper.selectById(newUser.getId());
        
        if (oldUser == null) {
            // If old data not found, show as insert
            return buildInsertSnapshot(newUser);
        }
        
        // Compare and mark changed fields
        fields.add(createCompareField("用户名", 
                oldUser.getUsername(), newUser.getUsername()));
        fields.add(createCompareField("姓名", 
                oldUser.getRealName(), newUser.getRealName()));
        fields.add(createCompareField("邮箱", 
                oldUser.getEmail(), newUser.getEmail()));
        fields.add(createCompareField("电话", 
                oldUser.getPhone(), newUser.getPhone()));
        fields.add(createCompareField("部门", 
                oldUser.getDepartment(), newUser.getDepartment()));
        
        // Status with badge
        String oldStatus = oldUser.getStatus() == 1 ? "启用" : "禁用";
        String newStatus = newUser.getStatus() == 1 ? "启用" : "禁用";
        DisplayField statusField = createCompareField("状态", oldStatus, newStatus);
        statusField.setBadge(newUser.getStatus() == 1 ? "success" : "secondary");
        fields.add(statusField);
        
        return SnapshotData.builder()
                .businessType("USER_MGMT")
                .operationType("UPDATE")
                .title("用户信息")
                .fields(fields)
                .summary(String.format("修改用户: %s (%s)", newUser.getRealName(), newUser.getUsername()))
                .build();
    }
    
    /**
     * Build snapshot for DELETE operation
     */
    private SnapshotData buildDeleteSnapshot(Long userId) {
        BizUser user = userMapper.selectById(userId);
        
        if (user == null) {
            return SnapshotData.builder()
                    .businessType("USER_MGMT")
                    .operationType("DELETE")
                    .title("用户信息")
                    .summary("用户不存在 (ID: " + userId + ")")
                    .build();
        }
        
        List<DisplayField> fields = new ArrayList<>();
        fields.add(new DisplayField("用户ID", String.valueOf(user.getId())));
        fields.add(new DisplayField("用户名", user.getUsername()));
        fields.add(new DisplayField("姓名", user.getRealName()));
        fields.add(new DisplayField("邮箱", user.getEmail() != null ? user.getEmail() : "-"));
        fields.add(new DisplayField("电话", user.getPhone() != null ? user.getPhone() : "-"));
        fields.add(new DisplayField("部门", user.getDepartment() != null ? user.getDepartment() : "-"));
        fields.add(new DisplayField("状态", 
                user.getStatus() == 1 ? "启用" : "禁用", 
                user.getStatus() == 1 ? "success" : "secondary"));
        
        return SnapshotData.builder()
                .businessType("USER_MGMT")
                .operationType("DELETE")
                .title("用户信息")
                .fields(fields)
                .summary(String.format("删除用户: %s (%s)", user.getRealName(), user.getUsername()))
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
    
    private SnapshotData buildEmptySnapshot(String operationType) {
        return SnapshotData.builder()
                .businessType("USER_MGMT")
                .operationType(operationType)
                .title("用户信息")
                .summary("无数据")
                .build();
    }
}
