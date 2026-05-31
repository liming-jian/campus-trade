package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.entity.*;
import com.campus_trade.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminManagementService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final OrderMapper orderMapper;
    private final ReportMapper reportMapper;
    private final ViolationMapper violationMapper;
    private final WarningMapper warningMapper;
    private final CategoryMapper categoryMapper;
    private final com.campus_trade.websocket.ChatWebSocketHandler wsHandler;

    public AdminManagementService(UserMapper userMapper, ProductMapper productMapper, ProductImageMapper productImageMapper, OrderMapper orderMapper, ReportMapper reportMapper, ViolationMapper violationMapper, WarningMapper warningMapper, CategoryMapper categoryMapper, com.campus_trade.websocket.ChatWebSocketHandler wsHandler) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.orderMapper = orderMapper;
        this.reportMapper = reportMapper;
        this.violationMapper = violationMapper;
        this.warningMapper = warningMapper;
        this.categoryMapper = categoryMapper;
        this.wsHandler = wsHandler;
    }

    // ---- Dashboard ----
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("productCount", productMapper.selectCount(null));
        data.put("orderCount", orderMapper.selectCount(null));

        LambdaQueryWrapper<Product> pendingReview = new LambdaQueryWrapper<>();
        pendingReview.eq(Product::getStatus, "REVIEW");
        data.put("pendingReviewCount", productMapper.selectCount(pendingReview));

        LambdaQueryWrapper<Report> pendingReports = new LambdaQueryWrapper<>();
        pendingReports.eq(Report::getStatus, "PENDING");
        data.put("pendingReportCount", reportMapper.selectCount(pendingReports));

        // Today's new: users, products, orders
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<User> todayUsers = new LambdaQueryWrapper<>();
        todayUsers.ge(User::getCreatedAt, todayStart);
        data.put("todayNewUsers", userMapper.selectCount(todayUsers));

        LambdaQueryWrapper<Product> todayProducts = new LambdaQueryWrapper<>();
        todayProducts.ge(Product::getCreatedAt, todayStart);
        data.put("todayNewProducts", productMapper.selectCount(todayProducts));

        LambdaQueryWrapper<Order> todayOrders = new LambdaQueryWrapper<>();
        todayOrders.ge(Order::getCreatedAt, todayStart);
        data.put("todayNewOrders", orderMapper.selectCount(todayOrders));

        return data;
    }

    // ---- User Management ----
    public Page<User> getUserPage(Integer page, Integer size, String keyword, Integer status, Integer verifyStatus) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getPhone, keyword).or().like(User::getNickname, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (verifyStatus != null) {
            wrapper.eq(User::getVerifyStatus, verifyStatus);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public void banUser(Long userId, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        user.setStatus(0);
        userMapper.updateById(user);

        Violation violation = new Violation();
        violation.setUserId(userId);
        violation.setType("USER");
        violation.setReason(reason);
        violation.setAction("BAN");
        violationMapper.insert(violation);

        Warning warning = new Warning();
        warning.setUserId(userId);
        warning.setTitle("账号封禁通知");
        warning.setContent("您的账号因" + reason + "已被封禁。如有疑问请联系管理员。");
        warningMapper.insert(warning);
    }

    public void unbanUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        user.setStatus(1);
        userMapper.updateById(user);
    }

    public void warnUser(Long userId, String title, String content) {
        Warning warning = new Warning();
        warning.setUserId(userId);
        warning.setTitle(title);
        warning.setContent(content);
        warningMapper.insert(warning);

        Violation violation = new Violation();
        violation.setUserId(userId);
        violation.setType("USER");
        violation.setReason(content);
        violation.setAction("WARN");
        violationMapper.insert(violation);

        wsHandler.pushToUser(userId, Map.of("type", "warning", "title", title, "content", content));
    }

    // ---- Product Audit ----
    public Page<Product> getProductAuditPage(Integer page, Integer size, String status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public void auditProduct(Long productId, String action, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new IllegalArgumentException("商品不存在");

        switch (action.toUpperCase()) {
            case "PASS":
                product.setStatus("PASS");
                wsHandler.pushToUser(product.getUserId(), Map.of("type", "audit", "productId", productId, "title", product.getTitle(), "result", "PASS", "reason", ""));
                break;
            case "REJECT":
                product.setStatus("REJECT");
                if (reason != null && !reason.isBlank()) {
                    Warning warning = new Warning();
                    warning.setUserId(product.getUserId());
                    warning.setTitle("商品审核未通过");
                    warning.setContent("您的商品\"" + product.getTitle() + "\"未通过审核。原因: " + reason);
                    warningMapper.insert(warning);
                }
                wsHandler.pushToUser(product.getUserId(), Map.of("type", "audit", "productId", productId, "title", product.getTitle(), "result", "REJECT", "reason", reason != null ? reason : ""));
                break;
            case "OFF":
                product.setStatus("OFF");
                if (reason != null && !reason.isBlank()) {
                    Warning warning = new Warning();
                    warning.setUserId(product.getUserId());
                    warning.setTitle("商品已被下架");
                    warning.setContent("您的商品\"" + product.getTitle() + "\"已被下架。原因: " + reason);
                    warningMapper.insert(warning);

                    Violation violation = new Violation();
                    violation.setUserId(product.getUserId());
                    violation.setProductId(productId);
                    violation.setType("PRODUCT");
                    violation.setReason(reason);
                    violation.setAction("DELIST");
                    violationMapper.insert(violation);
                }
                wsHandler.pushToUser(product.getUserId(), Map.of("type", "audit", "productId", productId, "title", product.getTitle(), "result", "OFF", "reason", reason != null ? reason : ""));
                break;
            default:
                throw new IllegalArgumentException("无效的审核动作");
        }
        productMapper.updateById(product);
    }

    // ---- Order Management ----
    public Page<Order> getOrderPage(Integer page, Integer size, String status, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword).or().like(Order::getProductTitle, keyword));
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ---- Report Management ----
    public Page<Report> getReportPage(Integer page, Integer size, String status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        return reportMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public void handleReport(Long reportId, Long handlerId, String action, String result) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new IllegalArgumentException("举报不存在");

        report.setHandlerId(handlerId);
        report.setHandleResult(result);

        switch (action.toUpperCase()) {
            case "RESOLVE":
                report.setStatus("RESOLVED");
                break;
            case "DISMISS":
                report.setStatus("DISMISSED");
                break;
            default:
                throw new IllegalArgumentException("无效的处理动作");
        }
        reportMapper.updateById(report);
    }

    // ---- Verify User ----
    public void verifyUser(Long userId, Integer verifyStatus, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        user.setVerifyStatus(verifyStatus);
        if (verifyStatus == 3 && reason != null) {
            user.setVerifyReason(reason);
            Warning warning = new Warning();
            warning.setUserId(userId);
            warning.setTitle("学生认证未通过");
            warning.setContent("您的学生认证申请未通过。原因: " + reason);
            warningMapper.insert(warning);
        }
        userMapper.updateById(user);
        String resultText = verifyStatus == 2 ? "PASS" : "REJECT";
        wsHandler.pushToUser(userId, Map.of("type", "verify", "result", resultText, "reason", reason != null ? reason : ""));
    }

    // ---- Product Audit with images ----
    public Page<Map<String, Object>> getProductAuditPageWithImages(Integer page, Integer size, String status) {
        Page<Product> productPage = getProductAuditPage(page, size, status);
        Page<Map<String, Object>> result = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Product p : productPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("title", p.getTitle());
            map.put("price", p.getPrice());
            map.put("status", p.getStatus());
            map.put("aiAuditResult", p.getAiAuditResult());
            map.put("aiAuditReason", p.getAiAuditReason());
            map.put("userId", p.getUserId());
            map.put("categoryId", p.getCategoryId());
            map.put("createdAt", p.getCreatedAt());
            LambdaQueryWrapper<ProductImage> imgW = new LambdaQueryWrapper<>();
            imgW.eq(ProductImage::getProductId, p.getId()).orderByAsc(ProductImage::getSortOrder);
            List<ProductImage> imgs = productImageMapper.selectList(imgW);
            map.put("images", imgs.stream().map(ProductImage::getUrl).toList());
            records.add(map);
        }
        result.setRecords(records);
        return result;
    }

    // ---- Warning List ----
    public List<Warning> getUserWarnings(Long userId) {
        LambdaQueryWrapper<Warning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warning::getUserId, userId).orderByDesc(Warning::getCreatedAt);
        return warningMapper.selectList(wrapper);
    }
}
