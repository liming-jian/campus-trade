package com.campus_trade.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressRequest {
    @NotBlank(message = "Receiver name cannot be blank")
    private String receiverName;

    @NotBlank(message = "Receiver phone cannot be blank")
    private String receiverPhone;

    @NotBlank(message = "Province cannot be blank")
    private String province;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "District cannot be blank")
    private String district;

    @NotBlank(message = "Detail address cannot be blank")
    private String detail;

    private Integer isDefault;

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return "AddressRequest{receiverName='" + receiverName + "', receiverPhone='" + receiverPhone + "'}";
    }
}
