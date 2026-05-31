package com.campus_trade.dto;

import jakarta.validation.constraints.NotEmpty;

public class SendCodeRequest {

    @NotEmpty(message = "手机号不能为空")
    private String phone;

    @NotEmpty(message = "验证码类型不能为空")
    private String type; // REGISTER, LOGIN, RESET_PWD

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "SendCodeRequest{phone='" + phone + "', type='" + type + "'}";
    }
}
