package com.JiuShuFang.goods.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "购物车 VO")
public class ShoppingCartVO {

    @Schema(description = "购物车商品列表")
    private List<ShoppingCartItemVO> items;

    @Schema(description = "总价")
    private BigDecimal totalPrice;
}
