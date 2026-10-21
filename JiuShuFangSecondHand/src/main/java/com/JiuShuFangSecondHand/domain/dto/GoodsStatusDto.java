package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "修改商品状态DTO")
public class GoodsStatusDto {

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long id;

    @Schema(description = "商品状态：1上架 2下架 3已售")
    @NotNull(message = "商品状态不能为空")
    private Integer status;
}
