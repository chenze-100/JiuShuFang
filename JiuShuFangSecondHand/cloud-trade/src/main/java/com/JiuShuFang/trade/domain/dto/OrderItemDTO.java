package com.JiuShuFang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "订单项DTO")
public class OrderItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long goodsId;

    @Min(value = 1, message = "数量不能小于1")
    @NotNull(message = "数量不能为空")
    @Schema(description = "数量")
    private Integer num;


}
