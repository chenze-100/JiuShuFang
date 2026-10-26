package com.JiuShuFang.trade.controller;

import com.JiuShuFang.trade.domain.dto.AddressDto;
import com.JiuShuFang.trade.domain.vo.AddressVO;
import com.JiuShuFang.trade.service.IAddressService;
import com.JiuShuFang.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户地址表 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@Tag(name = "地址", description = "地址相关接口")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @PostMapping
    public Result<String> addAddress(@Valid @RequestBody AddressDto dto) {
        addressService.addAddress(dto);
        return Result.success("地址添加成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success("地址删除成功");
    }

    @PutMapping
    public Result<String> updateAddress(@Valid @RequestBody AddressDto dto) {
        addressService.updateAddress(dto);
        return Result.success("地址修改成功");
    }

    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList() {
        List<AddressVO> result = addressService.getAddressList();
        return Result.success(result);
    }

    @PutMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id) {
        addressService.setDefaultAddress(id);
        return Result.success("设置默认地址成功");
    }

    @GetMapping("/{id}")
    public Result<AddressVO> getAddressById(@PathVariable Long id) {
        AddressVO addressVO = addressService.getAddressById(id);
        return Result.success(addressVO);
    }




}
