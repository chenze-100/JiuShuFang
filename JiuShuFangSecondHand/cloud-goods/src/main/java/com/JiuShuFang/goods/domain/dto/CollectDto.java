package com.JiuShuFang.goods.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "收藏DTO")
public class CollectDto {

    @Schema(description = "书籍ID")
    @NotNull(message = "书籍ID不能为空")
    private Long goodsId;

    @Schema(description = "是否收藏")
    @NotNull(message = "是否收藏不能为空")
    private Boolean isCollect;
}
