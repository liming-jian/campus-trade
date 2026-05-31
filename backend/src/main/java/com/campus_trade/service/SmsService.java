package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.entity.SmsCode;
import com.campus_trade.mapper.SmsCodeMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class SmsService {

    private final SmsCodeMapper smsCodeMapper;

    public SmsService(SmsCodeMapper smsCodeMapper) {
        this.smsCodeMapper = smsCodeMapper;
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public void sendCode(String phone, String type) {
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        System.out.println("========== 验证码: " + code + " ==========");

        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(type);
        smsCode.setUsed(0);
        smsCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        smsCodeMapper.insert(smsCode);
    }

    public boolean verifyCode(String phone, String code, String type) {
        LambdaQueryWrapper<SmsCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsCode::getPhone, phone)
               .eq(SmsCode::getCode, code)
               .eq(SmsCode::getType, type)
               .eq(SmsCode::getUsed, 0)
               .orderByDesc(SmsCode::getCreatedAt)
               .last("LIMIT 1");

        SmsCode smsCode = smsCodeMapper.selectOne(wrapper);
        if (smsCode == null) {
            return false;
        }

        if (smsCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        smsCode.setUsed(1);
        smsCodeMapper.updateById(smsCode);
        return true;
    }
}
