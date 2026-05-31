package com.campus_trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductRequest {
    @NotNull
    private Long categoryId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotBlank
    private String condition; // NEW, USED, OLD

    private Integer shippingFree = 1;

    private BigDecimal shippingFee = BigDecimal.ZERO;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Integer getShippingFree() { return shippingFree; }
    public void setShippingFree(Integer shippingFree) { this.shippingFree = shippingFree; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    @Override
    public String toString() {
        return "ProductRequest{categoryId=" + categoryId + ", title='" + title + "', price=" + price + "}";
    }
}
