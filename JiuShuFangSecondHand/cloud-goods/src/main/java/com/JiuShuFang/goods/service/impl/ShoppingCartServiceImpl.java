package com.JiuShuFang.goods.service.impl;

import com.JiuShuFang.goods.domain.dto.CartNumDto;
import com.JiuShuFang.goods.domain.dto.ShoppingCartDto;
import com.JiuShuFang.goods.domain.pojo.Goods;
import com.JiuShuFang.goods.domain.pojo.ShoppingCart;
import com.JiuShuFang.goods.domain.vo.ShoppingCartItemVO;
import com.JiuShuFang.goods.domain.vo.ShoppingCartVO;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.goods.exception.UnauthorizedException;
import com.JiuShuFang.goods.mapper.ShoppingCartMapper;
import com.JiuShuFang.goods.service.IGoodsService;
import com.JiuShuFang.goods.service.IShoppingCartService;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    private final IGoodsService goodsService;

    @Override
    public Result addShoppingCart(ShoppingCartDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        Goods goods = goodsService.getById(dto.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        if (goods.getStock() == null || goods.getStock() < dto.getNum()) {
            throw new BusinessException("商品库存不足");
        }

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getGoodsId, dto.getGoodsId())
                .eq(ShoppingCart::getIsDeleted, 0);
        ShoppingCart existCart = getOne(queryWrapper);

        if (existCart != null) {
            int newNum = dto.getNum() + existCart.getNum();
            BigDecimal totalPrice = goods.getPrice().multiply(new BigDecimal(newNum));
            existCart.setNum(newNum);
            existCart.setTotalPrice(totalPrice);
            updateById(existCart);
        } else {
            ShoppingCart shoppingCart = getShoppingCart(dto, goods, userId);
            save(shoppingCart);
        }

        return Result.success();
    }

    @Override
    public Result<ShoppingCartVO> getUserShoppingCart() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getIsDeleted, 0)
                .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = list(queryWrapper);

        List<ShoppingCartItemVO> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (ShoppingCart cart : shoppingCarts) {
            Goods goods = goodsService.getById(cart.getGoodsId());
            if (goods != null) {
                ShoppingCartItemVO itemVO = new ShoppingCartItemVO();
                itemVO.setId(cart.getId());
                itemVO.setGoodsId(cart.getGoodsId());
                itemVO.setPrice(cart.getPrice());
                itemVO.setNum(cart.getNum());
                itemVO.setTotalPrice(cart.getTotalPrice());
                itemVO.setBookName(goods.getBookName());
                itemVO.setBookImg(goods.getBookImg());
                items.add(itemVO);
                totalPrice = totalPrice.add(cart.getTotalPrice());
            }
        }

        ShoppingCartVO vo = new ShoppingCartVO();
        vo.setItems(items);
        vo.setTotalPrice(totalPrice);

        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result clearShoppingCart() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        lambdaUpdate().eq(ShoppingCart::getUserId, userId)
                .remove();

        return Result.success();
    }

    @Override
    public Result updateCartNum(CartNumDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        ShoppingCart cart = getById(dto.getCartId());
        if (cart == null || !cart.getUserId().equals(userId) || cart.getIsDeleted() == 1) {
            throw new BusinessException("购物车记录不存在");
        }

        Goods goods = goodsService.getById(cart.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        int newNum;
        if (dto.getIsPlus()) {
            newNum = cart.getNum() + 1;
            if (newNum > goods.getStock()) {
                throw new BusinessException("商品库存不足");
            }
        } else {
            newNum = cart.getNum() - 1;
            if (newNum <= 0) {
                removeById(cart.getId());
                return Result.success();
            }
        }

        BigDecimal totalPrice = goods.getPrice().multiply(new BigDecimal(newNum));
        cart.setNum(newNum);
        cart.setTotalPrice(totalPrice);
        updateById(cart);

        return Result.success();
    }

    @Override
    public Result deleteCartById(Long cartId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        ShoppingCart cart = getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId) || cart.getIsDeleted() == 1) {
            throw new BusinessException("购物车记录不存在");
        }

        removeById(cartId);
        return Result.success();
    }

    @NotNull
    private static ShoppingCart getShoppingCart(ShoppingCartDto dto, Goods goods, Long userId) {
        Integer num = dto.getNum();
        BigDecimal price = dto.getPrice() != null ? dto.getPrice() : goods.getPrice();
        BigDecimal totalPrice = price.multiply(new BigDecimal(num));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        shoppingCart.setGoodsId(dto.getGoodsId());
        shoppingCart.setPrice(price);
        shoppingCart.setNum(dto.getNum());
        shoppingCart.setTotalPrice(totalPrice);
        return shoppingCart;
    }
}
