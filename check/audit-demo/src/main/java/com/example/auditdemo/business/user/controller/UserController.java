package com.example.auditdemo.business.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auditdemo.business.user.entity.BizUser;
import com.example.auditdemo.business.user.service.UserService;
import com.example.auditdemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * User list page
     */
    @GetMapping
    public String listPage(Model model,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword) {
        Page<BizUser> pageResult = userService.getList(page, size, keyword);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        return "user/list";
    }

    /**
     * Add user page
     */
    @GetMapping("/add")
    public String addPage() {
        return "user/form";
    }

    /**
     * Edit user page
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        BizUser user = userService.getById(id);
        model.addAttribute("user", user);
        return "user/form";
    }

    /**
     * Get user by ID (API)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Result<BizUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * Add user (API) - triggers audit
     */
    @PostMapping("/api")
    @ResponseBody
    public Result<Void> addUser(@RequestBody BizUser user) {
        try {
            userService.addUser(user);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to add user", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update user (API) - triggers audit
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody BizUser user) {
        try {
            user.setId(id);
            userService.updateUser(user);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to update user", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Delete user (API) - triggers audit
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("已提交审核，请等待复核通过", null);
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            return Result.error(e.getMessage());
        }
    }
}
