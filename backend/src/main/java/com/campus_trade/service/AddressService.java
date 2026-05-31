package com.campus_trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus_trade.dto.AddressRequest;
import com.campus_trade.entity.Address;
import com.campus_trade.mapper.AddressMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<Address> list(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdatedAt);
        return addressMapper.selectList(wrapper);
    }

    public Address getById(Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new RuntimeException("Address not found");
        }
        return address;
    }

    public void verifyOwnership(Long id, Long userId) {
        Address address = getById(id);
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("No permission to operate this address");
        }
    }

    public Address create(Long userId, AddressRequest req) {
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            unsetDefaults(userId);
        }
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(req.getReceiverName());
        address.setReceiverPhone(req.getReceiverPhone());
        address.setProvince(req.getProvince());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setDetail(req.getDetail());
        address.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : 0);
        addressMapper.insert(address);
        return address;
    }

    public Address update(Long id, Long userId, AddressRequest req) {
        verifyOwnership(id, userId);
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            unsetDefaults(userId);
        }
        Address address = new Address();
        address.setId(id);
        address.setReceiverName(req.getReceiverName());
        address.setReceiverPhone(req.getReceiverPhone());
        address.setProvince(req.getProvince());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setDetail(req.getDetail());
        address.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : 0);
        addressMapper.updateById(address);
        return getById(id);
    }

    public void delete(Long id, Long userId) {
        verifyOwnership(id, userId);
        addressMapper.deleteById(id);
    }

    public void setDefault(Long id, Long userId) {
        verifyOwnership(id, userId);
        unsetDefaults(userId);
        Address address = new Address();
        address.setId(id);
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    private void unsetDefaults(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1);
        Address update = new Address();
        update.setIsDefault(0);
        addressMapper.update(update, wrapper);
    }
}
