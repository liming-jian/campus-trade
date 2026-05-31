package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.entity.Category;
import com.campus_trade.entity.Product;
import com.campus_trade.mapper.CategoryMapper;
import com.campus_trade.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryService(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    public List<Category> list() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return category;
    }

    public Category create(String name, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setSortOrder(0);
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Long id, String name, String icon) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        if (name != null) {
            category.setName(name);
        }
        if (icon != null) {
            category.setIcon(icon);
        }
        categoryMapper.updateById(category);
        return category;
    }

    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getCategoryId, id);
        long count = productMapper.selectCount(productWrapper);
        if (count > 0) {
            throw new RuntimeException("该分类下存在商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
