package com.JiuShuFang.goods.service;

import com.JiuShuFang.goods.domain.dto.CartNumDto;
import com.JiuShuFang.goods.domain.dto.ShoppingCartDto;
import com.JiuShuFang.goods.domain.pojo.ShoppingCart;
import com.JiuShuFang.goods.domain.vo.ShoppingCartVO;
import com.JiuShuFang.common.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface IShoppingCartService extends IService<ShoppingCart> {

    Result addShoppingCart(ShoppingCartDto dto);

    Result<ShoppingCartVO> getUserShoppingCart();

    Result clearShoppingCart();

    Result updateCartNum(CartNumDto dto);

    Result deleteCartById(Long cartId);
}
