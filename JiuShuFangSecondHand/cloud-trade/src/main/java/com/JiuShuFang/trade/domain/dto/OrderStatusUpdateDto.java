package com.JiuShuFang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "订单状态更新 DTO")
public class OrderStatusUpdateDto {

    @NotNull(message = "订单 ID 不能为空")
    @Schema(description = "订单 ID", required = true)
    private Long orderId;


    @NotNull(message = "用户 ID 不能为空")
    @Schema(description = "用户 ID", required = true)
    private Long userId;

    @NotNull(message = "当前订单项不能为空")
    @Schema(description = "当前订单项")
    private Long orderItemId;

    @NotNull(message = "订单状态不能为空")
    @Schema(description = "目标订单状态：2 待发货 3 待收货 4 已完成 5 已取消", required = true, allowableValues = {"2", "3", "4", "5"})
    private Integer status;

    @Schema(description = "取消原因（仅取消订单时需要）")
    private String cancelReason;
}
