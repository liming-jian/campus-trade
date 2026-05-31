package com.campus_trade.controller;

import com.campus_trade.common.Result;
import com.campus_trade.entity.Category;
import com.campus_trade.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.list();
        return Result.ok(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.ok(category);
    }

    @PostMapping
    public Result<Category> create(@RequestParam String name, @RequestParam(required = false) String icon) {
        Category category = categoryService.create(name, icon);
        return Result.ok(category);
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String icon) {
        Category category = categoryService.update(id, name, icon);
        return Result.ok(category);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
