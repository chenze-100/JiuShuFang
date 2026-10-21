package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论VO")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "书籍ID")
    private Long goodsId;

    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片")
    private List<String> picture;

    @Schema(description = "评分（1-5星）")
    private Integer score;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
