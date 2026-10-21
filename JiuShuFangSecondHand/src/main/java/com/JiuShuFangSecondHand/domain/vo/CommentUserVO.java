package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "我的评价 VO")
public class CommentUserVO {

    @Schema(description = "评论 ID")
    private Long id;

    @Schema(description = "书籍 ID")
    private Long goodsId;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "评论人 ID")
    private Long userId;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片")
    private List<String> picture;

    @Schema(description = "评分（1-5 星）")
    private Integer score;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "书籍名称")
    private String bookName;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "书籍封面图片")
    private String bookImg;
}
