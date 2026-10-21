package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.AddressDto;
import com.JiuShuFangSecondHand.domain.vo.AddressVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.JiuShuFangSecondHand.domain.pojo.Address;

import java.util.List;

/**
 * <p>
 * 用户地址表 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface IAddressService extends IService<Address> {

    /**
     * 新增地址
     *
     * @param dto 地址DTO
     */
    void addAddress(AddressDto dto);

    /**
     * 删除地址
     *
     * @param id 地址ID
     */
    void deleteAddress(Long id);

    /**
     * 修改地址
     *
     * @param dto 地址DTO
     */
    void updateAddress(AddressDto dto);

    /**
     * 分页查询地址
     *
     * @return 分页结果
     */
    List<AddressVO> getAddressList();

    /**
     * 设置默认地址
     *
     * @param id 地址 ID
     */
    void setDefaultAddress(Long id);

    /**
     * 根据 ID 获取地址详情
     *
     * @param id 地址 ID
     * @return 地址 VO
     */
    AddressVO getAddressById(Long id);


}
