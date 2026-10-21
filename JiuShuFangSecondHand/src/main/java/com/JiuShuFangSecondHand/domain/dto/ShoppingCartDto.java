package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "添加购物车 DTO")
public class ShoppingCartDto {

    @NotNull(message = "商品 ID 不能为空")
    @Schema(description = "商品 ID")
    private Long goodsId;

    @NotNull(message = "商品单价不能为空")
    @Schema(description = "商品单价")
    private BigDecimal price;

    @NotNull(message = "购买数量不能为空")
    @Schema(description = "购买数量")
    private Integer num = 1;
}
