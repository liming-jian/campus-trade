package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus_trade.dto.ProductDTO;
import com.campus_trade.entity.*;
import com.campus_trade.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final UserMapper userMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, ProductMapper productMapper, ProductImageMapper productImageMapper, UserMapper userMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public boolean toggle(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        Favorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            product.setFavoriteCount(Math.max(0, (product.getFavoriteCount() != null ? product.getFavoriteCount() : 1) - 1));
            productMapper.updateById(product);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteMapper.insert(favorite);
            product.setFavoriteCount((product.getFavoriteCount() != null ? product.getFavoriteCount() : 0) + 1);
            productMapper.updateById(product);
            return true;
        }
    }

    public boolean isFavorited(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    public IPage<ProductDTO> getMyFavorites(Long userId, Integer pageNum, Integer size) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> page = new Page<>(pageNum, size);
        favoriteMapper.selectPage(page, wrapper);

        List<ProductDTO> dtoList = page.getRecords().stream().map(fav -> {
            Product product = productMapper.selectById(fav.getProductId());
            if (product == null) {
                return null;
            }
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
            dto.setIsFavorited(true);

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
            }

            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());

        Page<ProductDTO> result = new Page<>(pageNum, size, page.getTotal());
        result.setRecords(dtoList);
        return result;
    }
}
