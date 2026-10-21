package com.JiuShuFangSecondHand.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @since 2026-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
@Schema(description = "ChatMessage对象")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "发送者ID")
    private Long fromId;


    @Schema(description = "接收者ID")
    private Long toId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型 1 text ,2 image, 3 order , 4 product")
    private Integer msgType;

    @Schema(description = "0未读 1已读")
    private Integer isRead;

    private LocalDateTime createTime;


}
