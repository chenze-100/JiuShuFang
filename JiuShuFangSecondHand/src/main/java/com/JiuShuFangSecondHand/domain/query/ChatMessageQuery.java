package com.JiuShuFangSecondHand.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "聊天消息查询参数")
public class ChatMessageQuery extends PageQuery {

    @Schema(description = "会话 ID")
    @NotNull(message = "会话 ID 不能为空")
    private Long sessionId;
}
