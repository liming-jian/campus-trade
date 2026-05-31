package com.campus_trade.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequest {
    @NotNull(message = "Address ID cannot be null")
    private Long addressId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private String remark;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "OrderRequest{addressId=" + addressId + ", productId=" + productId + "}";
    }
}
