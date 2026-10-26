package com.JiuShuFang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 秒杀下单消息（MQ 载荷）
 * </p>
 *
 * @since 2026-10-14
 */
@Data
@Schema(description = "秒杀下单消息")
public class SeckillOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "秒杀活动ID")
    private Long seckillId;

    @Schema(description = "抢购用户ID")
    private Long userId;
}
