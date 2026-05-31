package com.campus_trade.service;

import com.campus_trade.common.FileUploadUtils;
import com.campus_trade.dto.ProductRequest;
import com.campus_trade.mapper.CategoryMapper;
import com.campus_trade.mapper.FavoriteMapper;
import com.campus_trade.mapper.ProductImageMapper;
import com.campus_trade.mapper.ProductMapper;
import com.campus_trade.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class ProductServiceTest {

    @Test
    void createRejectsMoreThanNineImages() {
        ProductMapper productMapper = mock(ProductMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        ProductImageMapper productImageMapper = mock(ProductImageMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        CategoryMapper categoryMapper = mock(CategoryMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        UserMapper userMapper = mock(UserMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        FavoriteMapper favoriteMapper = mock(FavoriteMapper.class, withSettings().mockMaker("mock-maker-subclass"));
        FileUploadUtils fileUploadUtils = mock(FileUploadUtils.class, withSettings().mockMaker("mock-maker-subclass"));
        ProductService productService = new ProductService(productMapper, productImageMapper, categoryMapper, userMapper, favoriteMapper, fileUploadUtils);

        ProductRequest request = new ProductRequest();
        request.setCategoryId(1L);
        request.setTitle("测试商品");
        request.setDescription("测试描述");
        request.setPrice(BigDecimal.ONE);
        request.setCondition("NEW");
        MultipartFile[] images = new MultipartFile[10];

        assertThrows(IllegalArgumentException.class, () -> productService.create(1L, request, images));
    }
}
