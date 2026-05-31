package com.campus_trade.controller;

import com.campus_trade.common.Result;
import com.campus_trade.dto.*;
import com.campus_trade.service.SmsService;
import com.campus_trade.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SmsService smsService;

    public AuthController(UserService userService, SmsService smsService) {
        this.userService = userService;
        this.smsService = smsService;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    @PostMapping("/send-code")
    public Result<?> sendCode(@Valid @RequestBody SendCodeRequest req) {
        smsService.sendCode(req.getPhone(), req.getType());
        return Result.ok("验证码已发送");
    }

    @GetMapping("/me")
    public Result<UserDTO> me() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String nickname = body.get("nickname");
        return Result.ok(userService.updateProfile(userId, nickname));
    }

    @PutMapping("/avatar")
    public Result<UserDTO> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(userService.updateAvatar(userId, file));
    }

    @PostMapping("/verify")
    public Result<UserDTO> verifyStudent(@RequestParam("file") MultipartFile file,
                                          @ModelAttribute VerifyRequest req) throws IOException {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(userService.verifyStudent(userId, req, file));
    }

    @PostMapping("/reset-password")
    public Result<?> resetPassword(@Valid @RequestBody ResetPwdRequest req) {
        userService.resetPassword(req);
        return Result.ok("密码重置成功");
    }
}
