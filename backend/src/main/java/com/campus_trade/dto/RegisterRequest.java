package com.campus_trade.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的中国大陆手机号")
    private String phone;

    @NotEmpty(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能小于8位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).+$", message = "密码必须包含大小写字母和特殊符号")
    private String password;

    private String code; // optional, only when SMS is enabled

    private String nickname;

    public RegisterRequest() {}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    @Override
    public String toString() {
        return "RegisterRequest{phone='" + phone + "', password='***', code='" + code + "', nickname='" + nickname + "'}";
    }
}
