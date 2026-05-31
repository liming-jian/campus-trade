package com.campus_trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus_trade.common.Result;
import com.campus_trade.dto.OrderDTO;
import com.campus_trade.dto.OrderRequest;
import com.campus_trade.entity.Order;
import com.campus_trade.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public Result<OrderDTO> create(@Valid @RequestBody OrderRequest req) {
        Long userId = getCurrentUserId();
        Order order = orderService.create(userId, req);
        return Result.ok(toDTO(order));
    }

    @GetMapping
    public Result<IPage<OrderDTO>> getOrderList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        IPage<OrderDTO> result = orderService.getOrderList(userId, role, status, page, size);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<OrderDTO> getDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        OrderDTO dto = orderService.getDetail(id, userId);
        return Result.ok(dto);
    }

    @PutMapping("/{id}/pay")
    public Result<OrderDTO> pay(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderService.pay(id, userId);
        return Result.ok(toDTO(order));
    }

    @PutMapping("/{id}/ship")
    public Result<OrderDTO> ship(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderService.ship(id, userId);
        return Result.ok(toDTO(order));
    }

    @PutMapping("/{id}/confirm")
    public Result<OrderDTO> confirm(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderService.confirm(id, userId);
        return Result.ok(toDTO(order));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderDTO> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String reason = body.getOrDefault("reason", "");
        Order order = orderService.cancel(id, userId, reason);
        return Result.ok(toDTO(order));
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setBuyerId(order.getBuyerId());
        dto.setSellerId(order.getSellerId());
        dto.setProductId(order.getProductId());
        dto.setProductTitle(order.getProductTitle());
        dto.setProductPrice(order.getProductPrice());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setAddressSnapshot(order.getAddressSnapshot());
        dto.setPaidAt(order.getPaidAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
