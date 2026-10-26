package com.JiuShuFang.trade.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀书籍表
 * </p>
 *
 * @since 2026-10-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("seckill_book")
@Schema(description = "秒杀书籍表")
public class SeckillBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "秒杀活动ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "关联商品ID")
    private Long goodsId;

    @Schema(description = "书籍名称（快照）")
    private String bookName;

    @Schema(description = "封面图（快照）")
    private String bookImg;

    @Schema(description = "秒杀价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "初始库存（压测复位用）")
    private Integer initialStock;

    @Schema(description = "当前剩余库存（DB 侧权威值）")
    private Integer seckillStock;

    @Schema(description = "开抢时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：1进行中 2已结束")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除 0未删 1已删")
    private Integer isDeleted;
}
