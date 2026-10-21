package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.JiuShuFangSecondHand.domain.dto.AddressDto;
import com.JiuShuFangSecondHand.domain.vo.AddressVO;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.service.IAddressService;
import com.JiuShuFangSecondHand.util.BeanUtils;
import com.JiuShuFangSecondHand.util.UserContext;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.JiuShuFangSecondHand.domain.pojo.Address;
import com.JiuShuFangSecondHand.domain.pojo.User;
import com.JiuShuFangSecondHand.mapper.AddressMapper;
import com.JiuShuFangSecondHand.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 用户地址表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    @Override
    public void addAddress(AddressDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiver(dto.getReceiver());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());

        if (dto.getIsDefault()) {
            address.setIsDefault(true);
            lambdaUpdate()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDefault, true)
                    .set(Address::getIsDefault, false)
                    .update();
        } else {
            address.setIsDefault(false);
        }

        addressMapper.insert(address);
    }

    @Override
    public void deleteAddress(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        lambdaUpdate()
                .eq(Address::getId, id)
                .remove();
    }

    @Override
    public void updateAddress(AddressDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        Address address = lambdaQuery()
                .eq(Address::getId, dto.getId())
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDeleted, 0)
                .one();

        if (address == null) {
            throw new BusinessException("地址不存在");
        }

        address.setReceiver(dto.getReceiver());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());

        if (dto.getIsDefault()) {
            address.setIsDefault(true);
            lambdaUpdate()
                    .eq(Address::getIsDefault, true)
                    .eq(Address::getId, dto.getId())
                    .set(Address::getIsDefault, false)
                    .update();
        } else {
            address.setIsDefault(false);
        }

        updateById(address);
    }

    @Override
    public List<AddressVO> getAddressList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        List<Address> addresses = lambdaQuery()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDeleted, 0)
                .list();

        return BeanUtils.copyList(addresses, AddressVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefaultAddress(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        lambdaUpdate()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, true)
                .set(Address::getIsDefault, false)
                .update();

        lambdaUpdate()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, true)
                .update();
    }

    @Override
    public AddressVO getAddressById(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        Address address = lambdaQuery()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDeleted, 0)
                .one();

        if (address == null) {
            throw new BusinessException("地址不存在");
        }

        return BeanUtil.copyProperties(address, AddressVO.class);
    }



}
