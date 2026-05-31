package com.campus_trade.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPwdRequest {

    @NotEmpty(message = "手机号不能为空")
    private String phone;

    private String code;

    @NotEmpty(message = "新密码不能为空")
    @Size(min = 8, message = "密码长度不能小于8位")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).+$", message = "密码必须包含大小写字母和特殊符号")
    private String newPassword;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    @Override
    public String toString() {
        return "ResetPwdRequest{phone='" + phone + "', newPassword='***'}";
    }
}
