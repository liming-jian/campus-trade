package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.dto.OrderDTO;
import com.campus_trade.dto.OrderRequest;
import com.campus_trade.entity.*;
import com.campus_trade.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public OrderService(OrderMapper orderMapper, ProductMapper productMapper, ProductImageMapper productImageMapper, AddressMapper addressMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.addressMapper = addressMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order create(Long buyerId, OrderRequest req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        if (!"PASS".equals(product.getStatus())) {
            throw new RuntimeException("Product is not available for purchase");
        }
        if (buyerId.equals(product.getUserId())) {
            throw new RuntimeException("Cannot purchase your own product");
        }

        Address address = addressMapper.selectById(req.getAddressId());
        if (address == null || !address.getUserId().equals(buyerId)) {
            throw new RuntimeException("Address not found or no permission");
        }

        String orderNo = generateOrderNo();
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (product.getShippingFree() == null || product.getShippingFree() == 0) {
            shippingFee = product.getShippingFee() != null ? product.getShippingFee() : BigDecimal.ZERO;
        }
        BigDecimal totalAmount = product.getPrice().add(shippingFee);

        String addressJson;
        try {
            Map<String, Object> addrMap = new LinkedHashMap<>();
            addrMap.put("receiverName", address.getReceiverName());
            addrMap.put("receiverPhone", address.getReceiverPhone());
            addrMap.put("province", address.getProvince());
            addrMap.put("city", address.getCity());
            addrMap.put("district", address.getDistrict());
            addrMap.put("detail", address.getDetail());
            addressJson = objectMapper.writeValueAsString(addrMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize address");
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setProductPrice(product.getPrice());
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAY");
        order.setAddressSnapshot(addressJson);
        orderMapper.insert(order);
        return order;
    }

    public Order pay(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("No permission to pay this order");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new RuntimeException("Order status does not allow payment");
        }
        order.setStatus("PENDING_SHIP");
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    public Order ship(Long orderId, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("No permission to ship this order");
        }
        if (!"PENDING_SHIP".equals(order.getStatus())) {
            throw new RuntimeException("Order status does not allow shipping");
        }
        order.setStatus("PENDING_RECEIVE");
        order.setShippedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    @Transactional
    public Order confirm(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("No permission to confirm this order");
        }
        if (!"PENDING_RECEIVE".equals(order.getStatus())) {
            throw new RuntimeException("Order status does not allow confirmation");
        }
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Product product = new Product();
        product.setId(order.getProductId());
        product.setStatus("SOLD");
        productMapper.updateById(product);

        return order;
    }

    public Order cancel(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("No permission to cancel this order");
        }
        if (!"PENDING_PAY".equals(order.getStatus()) && !"PENDING_SHIP".equals(order.getStatus())) {
            throw new RuntimeException("Order status does not allow cancellation");
        }
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);
        return order;
    }

    public IPage<OrderDTO> getOrderList(Long userId, String role, String status, Integer page, Integer size) {
        Page<Order> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 10);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if ("BUYER".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("SELLER".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        Set<Long> userIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Order order : orderPage.getRecords()) {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
            productIds.add(order.getProductId());
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        Map<Long, List<String>> productImageMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
            imgWrapper.in(ProductImage::getProductId, productIds)
                    .orderByAsc(ProductImage::getSortOrder);
            List<ProductImage> images = productImageMapper.selectList(imgWrapper);
            for (ProductImage image : images) {
                productImageMap.computeIfAbsent(image.getProductId(), k -> new ArrayList<>()).add(image.getUrl());
            }
        }

        IPage<OrderDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        dtoPage.setRecords(orderPage.getRecords().stream().map(order -> {
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

            User seller = userMap.get(order.getSellerId());
            if (seller != null) {
                dto.setSellerNickname(seller.getNickname());
                dto.setSellerAvatar(seller.getAvatar());
            }
            User buyer = userMap.get(order.getBuyerId());
            if (buyer != null) {
                dto.setBuyerNickname(buyer.getNickname());
            }

            List<String> imgList = productImageMap.getOrDefault(order.getProductId(), Collections.emptyList());
            dto.setProductImages(imgList.isEmpty() ? Collections.emptyList() : Collections.singletonList(imgList.get(0)));

            return dto;
        }).collect(Collectors.toList()));
        return dtoPage;
    }

    public OrderDTO getDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("No permission to view this order");
        }

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

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            dto.setSellerNickname(seller.getNickname());
            dto.setSellerAvatar(seller.getAvatar());
        }
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            dto.setBuyerNickname(buyer.getNickname());
        }

        LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(ProductImage::getProductId, order.getProductId())
                .orderByAsc(ProductImage::getSortOrder);
        List<ProductImage> images = productImageMapper.selectList(imgWrapper);
        List<String> imgUrls = images.stream().map(ProductImage::getUrl).collect(Collectors.toList());
        dto.setProductImages(imgUrls);

        return dto;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return timestamp + random;
    }
}
