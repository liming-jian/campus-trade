package com.campus_trade.controller;

import com.campus_trade.common.Result;
import com.campus_trade.dto.AdminLoginRequest;
import com.campus_trade.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest req) {
        return Result.ok(adminService.login(req));
    }
}
