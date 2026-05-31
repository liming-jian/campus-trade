package com.campus_trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus_trade.common.Result;
import com.campus_trade.dto.ProductDTO;
import com.campus_trade.service.FavoriteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long productId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean favorited = favoriteService.toggle(userId, productId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.ok(data);
    }

    @GetMapping
    public Result<IPage<ProductDTO>> getMyFavorites(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        IPage<ProductDTO> result = favoriteService.getMyFavorites(userId, page, size);
        return Result.ok(result);
    }
}
