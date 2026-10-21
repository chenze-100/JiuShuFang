package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
@Schema(description = "发表评论DTO")
public class CommentDto {

    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "书籍ID")
    @NotNull(message = "书籍ID不能为空")
    private Long goodsId;

    @Schema(description = "评论内容")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图")
    private List<String> picture;

    @Schema(description = "评分（1-5星）")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer score;


    private Long userId;
}
