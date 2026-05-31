package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.common.FileUploadUtils;
import com.campus_trade.dto.ProductDTO;
import com.campus_trade.dto.ProductRequest;
import com.campus_trade.entity.*;
import com.campus_trade.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final FileUploadUtils fileUploadUtils;

    public ProductService(ProductMapper productMapper, ProductImageMapper productImageMapper, CategoryMapper categoryMapper, UserMapper userMapper, FavoriteMapper favoriteMapper, FileUploadUtils fileUploadUtils) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.fileUploadUtils = fileUploadUtils;
    }

    @Transactional
    public Long create(Long userId, ProductRequest req, MultipartFile[] images) {
        if (images != null && images.length > 9) {
            throw new IllegalArgumentException("最多只能上传9张图片");
        }

        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(req.getCategoryId());
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setCondition(req.getCondition());
        product.setShippingFree(req.getShippingFree() != null ? req.getShippingFree() : 1);
        product.setShippingFee(req.getShippingFee() != null ? req.getShippingFee() : java.math.BigDecimal.ZERO);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setStatus("REVIEW");
        productMapper.insert(product);

        if (images != null && images.length > 0) {
            int sortOrder = 0;
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String url = fileUploadUtils.upload(file);
                        ProductImage productImage = new ProductImage();
                        productImage.setProductId(product.getId());
                        productImage.setUrl(url);
                        productImage.setSortOrder(sortOrder++);
                        productImageMapper.insert(productImage);
                    } catch (IOException e) {
                        throw new RuntimeException("图片上传失败: " + e.getMessage());
                    }
                }
            }
        }

        return product.getId();
    }

    public IPage<ProductDTO> getPage(Integer pageNum, Integer size, Long categoryId, String keyword, String sort) {
        Page<Product> page = new Page<>(pageNum, size);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "PASS");

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }

        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else if ("popular".equals(sort)) {
            wrapper.orderByDesc(Product::getViewCount);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        productMapper.selectPage(page, wrapper);

        List<ProductDTO> dtoList = page.getRecords().stream()
                .map(p -> toDTO(p, null))
                .collect(Collectors.toList());

        Page<ProductDTO> result = new Page<>(pageNum, size, page.getTotal());
        result.setRecords(dtoList);
        return result;
    }

    public ProductDTO getById(Long id, Long currentUserId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setViewCount((product.getViewCount() != null ? product.getViewCount() : 0) + 1);
        productMapper.updateById(product);

        return toDTO(product, currentUserId);
    }

    @Transactional
    public void update(Long productId, Long userId, ProductRequest req) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该商品");
        }

        product.setCategoryId(req.getCategoryId());
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setCondition(req.getCondition());
        product.setShippingFree(req.getShippingFree() != null ? req.getShippingFree() : 1);
        product.setShippingFee(req.getShippingFee() != null ? req.getShippingFee() : java.math.BigDecimal.ZERO);
        product.setStatus("REVIEW");

        productMapper.updateById(product);
    }

    @Transactional
    public void delete(Long productId, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该商品");
        }

        product.setStatus("OFF");
        productMapper.updateById(product);
    }

    public IPage<ProductDTO> getMyProducts(Long userId, Integer pageNum, Integer size) {
        Page<Product> page = new Page<>(pageNum, size);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);
        wrapper.ne(Product::getStatus, "OFF");
        wrapper.orderByDesc(Product::getCreatedAt);

        productMapper.selectPage(page, wrapper);

        List<ProductDTO> dtoList = page.getRecords().stream()
                .map(p -> toDTO(p, userId))
                .collect(Collectors.toList());

        Page<ProductDTO> result = new Page<>(pageNum, size, page.getTotal());
        result.setRecords(dtoList);
        return result;
    }

    private ProductDTO toDTO(Product product, Long currentUserId) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setUserId(product.getUserId());
        dto.setCategoryId(product.getCategoryId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCondition(product.getCondition());
        dto.setShippingFree(product.getShippingFree());
        dto.setShippingFee(product.getShippingFee());
        dto.setViewCount(product.getViewCount());
        dto.setFavoriteCount(product.getFavoriteCount());
        dto.setStatus(product.getStatus());
        dto.setAiAuditResult(product.getAiAuditResult());
        dto.setCreatedAt(product.getCreatedAt());

        // images
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getId());
        imageWrapper.orderByAsc(ProductImage::getSortOrder);
        List<ProductImage> images = productImageMapper.selectList(imageWrapper);
        dto.setImages(images.stream().map(ProductImage::getUrl).collect(Collectors.toList()));

        // seller info
        User seller = userMapper.selectById(product.getUserId());
        if (seller != null) {
            dto.setSellerNickname(seller.getNickname());
            dto.setSellerAvatar(seller.getAvatar());
            dto.setSellerSchoolName(seller.getSchoolName());
            dto.setSellerVerified(seller.getVerifyStatus() != null && seller.getVerifyStatus() == 2);
        }

        // category name
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            dto.setCategoryName(category.getName());
        }

        // isFavorited
        if (currentUserId != null) {
            LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(Favorite::getUserId, currentUserId);
            favoriteWrapper.eq(Favorite::getProductId, product.getId());
            dto.setIsFavorited(favoriteMapper.selectCount(favoriteWrapper) > 0);
        } else {
            dto.setIsFavorited(false);
        }

        return dto;
    }
}
