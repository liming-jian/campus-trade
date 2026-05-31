package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.config.JwtUtils;
import com.campus_trade.dto.AdminLoginRequest;
import com.campus_trade.entity.Admin;
import com.campus_trade.mapper.AdminMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AdminService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> login(AdminLoginRequest req) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, req.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(admin.getId(), admin.getUsername(), "ADMIN");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", admin);
        return result;
    }

    public Admin getById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }
        return admin;
    }
}
