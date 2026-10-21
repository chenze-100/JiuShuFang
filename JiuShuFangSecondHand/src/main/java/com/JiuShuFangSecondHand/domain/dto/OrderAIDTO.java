package com.JiuShuFangSecondHand.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderAIDTO {
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotNull(message = "地址ID不能为空")
    @Schema(description = "地址ID")
    private Long addressId;

    @NotNull(message = "对话ID不能为空")
    @Schema(description = "对话ID")
    private String conversationId;
}
