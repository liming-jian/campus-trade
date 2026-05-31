package com.campus_trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String condition;
    private Integer shippingFree;
    private BigDecimal shippingFee;
    private Integer viewCount;
    private Integer favoriteCount;
    private String status;
    private String aiAuditResult;
    private LocalDateTime createdAt;

    private List<String> images;
    private String sellerNickname;
    private String sellerAvatar;
    private String sellerSchoolName;
    private Boolean sellerVerified;
    private String categoryName;
    private Boolean isFavorited;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAiAuditResult() { return aiAuditResult; }
    public void setAiAuditResult(String aiAuditResult) { this.aiAuditResult = aiAuditResult; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getSellerNickname() { return sellerNickname; }
    public void setSellerNickname(String sellerNickname) { this.sellerNickname = sellerNickname; }
    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }
    public String getSellerSchoolName() { return sellerSchoolName; }
    public void setSellerSchoolName(String sellerSchoolName) { this.sellerSchoolName = sellerSchoolName; }
    public Boolean getSellerVerified() { return sellerVerified; }
    public void setSellerVerified(Boolean sellerVerified) { this.sellerVerified = sellerVerified; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Boolean getIsFavorited() { return isFavorited; }
    public void setIsFavorited(Boolean isFavorited) { this.isFavorited = isFavorited; }

    @Override
    public String toString() {
        return "ProductDTO{id=" + id + ", title='" + title + "', price=" + price + "}";
    }
}
