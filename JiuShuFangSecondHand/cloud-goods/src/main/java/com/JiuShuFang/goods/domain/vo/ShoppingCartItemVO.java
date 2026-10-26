package com.JiuShuFang.goods.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "购物车商品项 VO")
public class ShoppingCartItemVO {

    @Schema(description = "购物车 ID")
    private Long id;

    @Schema(description = "商品 ID")
    private Long goodsId;

    @Schema(description = "商品单价")
    private BigDecimal price;

    @Schema(description = "购买数量")
    private Integer num;

    @Schema(description = "小计总价")
    private BigDecimal totalPrice;

    @Schema(description = "书籍名称")
    private String bookName;

    @Schema(description = "书籍封面图片")
    private String bookImg;
}
