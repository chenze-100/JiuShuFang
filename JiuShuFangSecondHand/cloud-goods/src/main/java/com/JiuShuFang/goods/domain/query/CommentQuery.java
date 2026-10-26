package com.JiuShuFang.goods.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论分页查询参数")
public class CommentQuery extends PageQuery {

    @NotNull(message = "书籍ID不能为空")
    @Schema(description = "书籍ID")
    private Long goodsId;

    @Schema(description = "评分（1-5星）")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer score;
}
