package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.common.FileUploadUtils;
import com.campus_trade.config.JwtUtils;
import com.campus_trade.dto.*;
import com.campus_trade.entity.User;
import com.campus_trade.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FileUploadUtils fileUploadUtils;

    public UserService(UserMapper userMapper, SmsService smsService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, FileUploadUtils fileUploadUtils) {
        this.userMapper = userMapper;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.fileUploadUtils = fileUploadUtils;
    }

    public Map<String, Object> register(RegisterRequest req) {
        if (req.getCode() != null && !req.getCode().isEmpty()) {
            if (!smsService.verifyCode(req.getPhone(), req.getCode(), "REGISTER")) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, req.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null && !req.getNickname().isEmpty()
                ? req.getNickname()
                : "用户" + req.getPhone().substring(7));
        user.setVerifyStatus(0);
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtUtils.generateToken(user.getId(), user.getPhone(), "USER");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", toDTO(user));
        return result;
    }

    public Map<String, Object> login(LoginRequest req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, req.getPhone());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("手机号未注册");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("该账号已被封禁");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getPhone(), "USER");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", toDTO(user));
        return result;
    }

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    public UserDTO getCurrentUser(Long userId) {
        User user = getById(userId);
        return toDTO(user);
    }

    public UserDTO updateProfile(Long userId, String nickname) {
        User user = getById(userId);
        user.setNickname(nickname);
        userMapper.updateById(user);
        return toDTO(user);
    }

    public UserDTO updateAvatar(Long userId, MultipartFile file) throws IOException {
        User user = getById(userId);
        String newAvatar = fileUploadUtils.upload(file);
        String oldAvatar = user.getAvatar();
        user.setAvatar(newAvatar);
        userMapper.updateById(user);
        if (oldAvatar != null && !oldAvatar.isEmpty()) {
            fileUploadUtils.delete(oldAvatar);
        }
        return toDTO(user);
    }

    public UserDTO verifyStudent(Long userId, VerifyRequest req, MultipartFile studentCardImg) throws IOException {
        User user = getById(userId);
        String imgUrl = fileUploadUtils.upload(studentCardImg);
        user.setSchoolName(req.getSchoolName());
        user.setStudentId(req.getStudentId());
        user.setStudentCardImg(imgUrl);
        user.setVerifyStatus(1);
        userMapper.updateById(user);
        return toDTO(user);
    }

    public void resetPassword(ResetPwdRequest req) {
        // SMS verification only when code is provided (dev mode skips)
        if (req.getCode() != null && !req.getCode().isEmpty()) {
            if (!smsService.verifyCode(req.getPhone(), req.getCode(), "RESET_PWD")) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, req.getPhone());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("该手机号未注册");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setSchoolName(user.getSchoolName());
        dto.setStudentId(user.getStudentId());
        dto.setVerifyStatus(user.getVerifyStatus());
        dto.setVerifyReason(user.getVerifyReason());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
