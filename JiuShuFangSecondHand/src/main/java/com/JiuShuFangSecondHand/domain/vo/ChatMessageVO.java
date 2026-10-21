package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "聊天消息 VO")
public class ChatMessageVO {

    @Schema(description = "消息 ID")
    private Long id;

    @Schema(description = "会话 ID")
    private Long sessionId;

    @Schema(description = "发送者 ID")
    private Long fromId;

    @Schema(description = "发送者昵称")
    private String fromNickName;

    @Schema(description = "发送者头像")
    private String fromAvator;

    @Schema(description = "接收者 ID")
    private Long toId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型 1 text ,2 image, 3 order , 4 product")
    private Integer msgType;

    @Schema(description = "0未读 1已读")
    private Integer isRead;

    @Schema(description = "商品 ID")
    private Long goodsId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品价格")
    private String productPrice;

    private LocalDateTime createTime;
}
