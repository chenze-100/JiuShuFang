package com.JiuShuFang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 秒杀结果 VO（前端轮询用）
 * </p>
 *
 * @since 2026-10-14
 */
@Data
@Schema(description = "秒杀结果")
public class SeckillResultVO {

    @Schema(description = "结果状态：PROCESSING/SUCCESS/FAIL/SOLD_OUT/REPEAT/NONE")
    private String status;

    @Schema(description = "结果中文描述")
    private String message;

    @Schema(description = "秒杀订单ID（成功时有值）")
    private Long seckillOrderId;
}
