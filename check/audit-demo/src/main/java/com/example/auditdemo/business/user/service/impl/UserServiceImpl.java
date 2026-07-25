package com.example.auditdemo.business.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.audit.annotation.AuditRequired;
import com.example.auditdemo.permission.annotation.QueryPermission;
import com.example.auditdemo.business.user.entity.BizUser;
import com.example.auditdemo.business.user.mapper.BizUserMapper;
import com.example.auditdemo.business.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * User service implementation demonstrating single table audit.
 * 
 * Note: The @AuditRequired annotation will intercept the method.
 * The actual database operation will only execute after audit approval.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private BizUserMapper userMapper;

    @Override
    @QueryPermission(table = "biz_user")
    public Page<BizUser> getList(int page, int size, String keyword) {
        LambdaQueryWrapper<BizUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BizUser::getUsername, keyword)
                    .or().like(BizUser::getRealName, keyword)
                    .or().like(BizUser::getEmail, keyword);
        }
        wrapper.orderByDesc(BizUser::getId);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @QueryPermission(table = "biz_user")
    public BizUser getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * Add user - with audit review.
     * The insert will only execute after audit approval.
     */
    @Override
    @AuditRequired(businessType = "USER_MGMT", desc = "Add User")
    public void addUser(BizUser user) {
        // Normal business logic - this code executes AFTER audit approval
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("User inserted: {}", user.getUsername());
    }

    /**
     * Update user - with audit review.
     * The update will only execute after audit approval.
     */
    @Override
    @AuditRequired(businessType = "USER_MGMT", desc = "Update User")
    public void updateUser(BizUser user) {
        // Normal business logic - this code executes AFTER audit approval
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User updated: {}", user.getUsername());
    }

    /**
     * Delete user - with audit review.
     * The delete will only execute after audit approval.
     */
    @Override
    @AuditRequired(businessType = "USER_MGMT", desc = "Delete User")
    public void deleteUser(Long id) {
        // Normal business logic - this code executes AFTER audit approval
        userMapper.deleteById(id);
        log.info("User deleted: id={}", id);
    }
}
