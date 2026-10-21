package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.CartNumDto;
import com.JiuShuFangSecondHand.domain.dto.ShoppingCartDto;
import com.JiuShuFangSecondHand.domain.pojo.ShoppingCart;
import com.JiuShuFangSecondHand.domain.vo.ShoppingCartVO;
import com.JiuShuFangSecondHand.util.Result;
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
