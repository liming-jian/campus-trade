package com.campus_trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.common.Result;
import com.campus_trade.entity.*;
import com.campus_trade.service.AdminManagementService;
import com.campus_trade.service.CategoryService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminManagementController {

    private final AdminManagementService adminService;
    private final CategoryService categoryService;

    public AdminManagementController(AdminManagementService adminService, CategoryService categoryService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
    }

    private Long currentAdminId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ---- Dashboard ----
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(adminService.dashboard());
    }

    // ---- User Management ----
    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer verifyStatus) {
        return Result.ok(adminService.getUserPage(page, size, keyword, status, verifyStatus));
    }

    @PostMapping("/users/{id}/ban")
    public Result<?> banUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "违规行为");
        adminService.banUser(id, reason);
        return Result.ok();
    }

    @PostMapping("/users/{id}/unban")
    public Result<?> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return Result.ok();
    }

    @PostMapping("/users/{id}/warn")
    public Result<?> warnUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.warnUser(id, body.getOrDefault("title", "警告"), body.getOrDefault("content", ""));
        return Result.ok();
    }

    @GetMapping("/users/{id}/warnings")
    public Result<?> getUserWarnings(@PathVariable Long id) {
        return Result.ok(adminService.getUserWarnings(id));
    }

    // ---- Verify User ----
    @PostMapping("/users/{id}/verify")
    public Result<?> verifyUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer verifyStatus = (Integer) body.get("verifyStatus");
        String reason = (String) body.getOrDefault("reason", "");
        adminService.verifyUser(id, verifyStatus, reason);
        return Result.ok();
    }

    // ---- Product Audit ----
    @GetMapping("/products")
    public Result<?> getAuditProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.ok(adminService.getProductAuditPageWithImages(page, size, status));
    }

    @PostMapping("/products/{id}/audit")
    public Result<?> auditProduct(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String reason = body.getOrDefault("reason", "");
        adminService.auditProduct(id, action, reason);
        return Result.ok();
    }

    // ---- Order Management ----
    @GetMapping("/orders")
    public Result<Page<Order>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.getOrderPage(page, size, status, keyword));
    }

    // ---- Report Management ----
    @GetMapping("/reports")
    public Result<Page<Report>> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.ok(adminService.getReportPage(page, size, status));
    }

    @PostMapping("/reports/{id}/handle")
    public Result<?> handleReport(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String result = body.getOrDefault("result", "");
        adminService.handleReport(id, currentAdminId(), action, result);
        return Result.ok();
    }
}
