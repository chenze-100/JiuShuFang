package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀书籍详情 VO（含 Redis 实时库存与当前用户参与状态）
 * </p>
 *
 * @since 2026-10-14
 */
@Data
@Schema(description = "秒杀书籍详情")
public class SeckillBookVO {

    @Schema(description = "秒杀活动ID")
    private Long id;

    @Schema(description = "关联商品ID")
    private Long goodsId;

    @Schema(description = "书籍名称")
    private String bookName;

    @Schema(description = "封面图")
    private String bookImg;

    @Schema(description = "秒杀价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "Redis 侧实时剩余库存")
    private Integer remainStock;

    @Schema(description = "开抢时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：1进行中 2已结束")
    private Integer status;

    @Schema(description = "当前用户是否已抢到")
    private Boolean joined;

    @Schema(description = "当前用户是否在途处理中")
    private Boolean processing;
}
