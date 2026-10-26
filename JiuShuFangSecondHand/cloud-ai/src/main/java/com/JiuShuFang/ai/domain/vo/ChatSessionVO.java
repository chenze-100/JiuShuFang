package com.JiuShuFang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "聊天会话 VO")
public class ChatSessionVO {

    @Schema(description = "会话 ID")
    private Long id;

    @Schema(description = "对方用户名")
    private String name;

    @Schema(description = "对方头像")
    private String avator;

    @Schema(description = "对方 ID")
    private Long userId;

    @Schema(description = "关联商品 ID")
    private Long productId;

    @Schema(description = "关联订单 ID")
    private Long orderId;

    @Schema(description = "最后一条消息")
    private String lastMsg;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastTime;

    @Schema(description = "未读消息条数")
    private Integer notReadNum;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品价格")
    private String price;

    private LocalDateTime createdAt;
}
