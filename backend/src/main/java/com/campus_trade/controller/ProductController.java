package com.campus_trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.common.Result;
import com.campus_trade.dto.ProductDTO;
import com.campus_trade.dto.ProductRequest;
import com.campus_trade.entity.Report;
import com.campus_trade.mapper.ReportMapper;
import com.campus_trade.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ReportMapper reportMapper;

    public ProductController(ProductService productService, ReportMapper reportMapper) {
        this.productService = productService;
        this.reportMapper = reportMapper;
    }

    @GetMapping
    public Result<IPage<ProductDTO>> getPage(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sort) {
        IPage<ProductDTO> result = productService.getPage(page, size, categoryId, keyword, sort);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<ProductDTO> getById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ProductDTO product = productService.getById(id, currentUserId);
        return Result.ok(product);
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestPart ProductRequest request,
                               @RequestPart(required = false) MultipartFile[] images) {
        Long userId = getCurrentUserIdOrThrow();
        Long productId = productService.create(userId, request, images);
        return Result.ok(productId);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Long userId = getCurrentUserIdOrThrow();
        productService.update(id, userId, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrThrow();
        productService.delete(id, userId);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<IPage<ProductDTO>> getMyProducts(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserIdOrThrow();
        IPage<ProductDTO> result = productService.getMyProducts(userId, page, size);
        return Result.ok(result);
    }

    @GetMapping("/reports/my")
    public Result<IPage<Report>> getMyReports(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserIdOrThrow();
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, userId).orderByDesc(Report::getCreatedAt);
        IPage<Report> result = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @PostMapping("/{id}/report")
    public Result<?> reportProduct(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserIdOrThrow();
        String reason = body.getOrDefault("reason", "用户举报");
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType("PRODUCT");
        report.setTargetId(id);
        report.setReason(reason);
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return Result.ok();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    private Long getCurrentUserIdOrThrow() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }
}
