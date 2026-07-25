package com.example.auditdemo.business.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.business.user.entity.BizUser;

/**
 * User service interface
 */
public interface UserService {

    /**
     * Get user list with pagination
     */
    Page<BizUser> getList(int page, int size, String keyword);

    /**
     * Get user by ID
     */
    BizUser getById(Long id);

    /**
     * Add user (with audit)
     */
    void addUser(BizUser user);

    /**
     * Update user (with audit)
     */
    void updateUser(BizUser user);

    /**
     * Delete user (with audit)
     */
    void deleteUser(Long id);
}
