package com.JiuShuFang.goods.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MqCommentScoreDto {

    @Schema(description = "商品id")
    private Long goodsId;
    @Schema(description = "需要增加的评分")
    private Integer score;
    @Schema(description = "需要增加的点赞数")
    private Integer plus;
}
