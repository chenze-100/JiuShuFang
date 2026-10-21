package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "购物车数量变更 DTO")
public class CartNumDto {

    @NotNull(message = "购物车 ID 不能为空")
    @Schema(description = "购物车 ID")
    private Long cartId;

    @NotNull(message = "是否增加不能为空")
    @Schema(description = "是否增加 true-增加 false-减少")
    private Boolean isPlus;
}
