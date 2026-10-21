package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.ShopAddDto;
import com.JiuShuFangSecondHand.domain.vo.ShopVO;

import java.util.List;

/**
 * 商铺服务接口
 */
public interface IShopService {

    /**
     * 添加商铺（流程图核心逻辑）
     *
     * @param dto 商铺信息
     * @return 新创建的商铺ID
     */
    Long addShop(ShopAddDto dto);

    /**
     * 查询当前用户的商铺列表
     */
    List<ShopVO> listMyShops();

    /**
     * 根据ID查询商铺详情
     */
    ShopVO getById(Long shopId);

    /**
     * 检查当前用户是否已实名认证
     */
    boolean isRealNameVerified();

    /**
     * 检查当前用户是否有待审核商铺
     */
    boolean hasPendingShop();

    /**
     * 查询当前用户商铺数量
     */
    int countMyShops();
}
