package com.JiuShuFang.goods.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.JiuShuFang.goods.domain.dto.CartNumDto;
import com.JiuShuFang.goods.domain.dto.ShoppingCartDto;
import com.JiuShuFang.goods.domain.vo.ShoppingCartVO;
import com.JiuShuFang.goods.service.IShoppingCartService;
import com.JiuShuFang.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * <p>
 * 购物车表 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@Slf4j
@Tag(name = "购物车", description = "购物车相关接口")
@RestController
@RequestMapping("/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final IShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public Result addShoppingCart(@RequestBody @Valid ShoppingCartDto dto) {
        return shoppingCartService.addShoppingCart(dto);
    }

    /**
     * 获取用户购物车列表
     */
    @Operation(summary = "查询addShoppingCart")
    @GetMapping("/list")
    public Result<ShoppingCartVO> getUserShoppingCart() {
        return shoppingCartService.getUserShoppingCart();
    }


    /**
     *
     * @return
     */
    @DeleteMapping("/clear")
    public Result clearShoppingCart() {
        return shoppingCartService.clearShoppingCart();
    }


    /**
     * 修改购物车商品数量
     * @param dto
     * @return
     */
    @PutMapping("/num")
    public Result updateCartNum(@RequestBody @Valid CartNumDto dto) {
        return shoppingCartService.updateCartNum(dto);
    }

    /**
     * 根据购物车 ID 删除
     * @param cartId
     * @return
     */
    @Operation(summary = "删除updateCartNum")
    @DeleteMapping("/delete/{cartId}")
    public Result deleteCartById(@PathVariable Long cartId) {
        return shoppingCartService.deleteCartById(cartId);
    }
}
