package com.campus_trade.controller;

import com.campus_trade.common.Result;
import com.campus_trade.dto.AddressDTO;
import com.campus_trade.dto.AddressRequest;
import com.campus_trade.entity.Address;
import com.campus_trade.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public Result<List<AddressDTO>> list() {
        Long userId = getCurrentUserId();
        List<Address> list = addressService.list(userId);
        List<AddressDTO> dtoList = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Result.ok(dtoList);
    }

    @GetMapping("/{id}")
    public Result<AddressDTO> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressService.verifyOwnership(id, userId);
        Address address = addressService.getById(id);
        return Result.ok(toDTO(address));
    }

    @PostMapping
    public Result<AddressDTO> create(@Valid @RequestBody AddressRequest req) {
        Long userId = getCurrentUserId();
        Address address = addressService.create(userId, req);
        return Result.ok(toDTO(address));
    }

    @PutMapping("/{id}")
    public Result<AddressDTO> update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        Long userId = getCurrentUserId();
        Address address = addressService.update(id, userId, req);
        return Result.ok(toDTO(address));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressService.delete(id, userId);
        return Result.ok();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressService.setDefault(id, userId);
        return Result.ok();
    }

    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUserId());
        dto.setReceiverName(address.getReceiverName());
        dto.setReceiverPhone(address.getReceiverPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetail(address.getDetail());
        dto.setIsDefault(address.getIsDefault());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        return dto;
    }
}
