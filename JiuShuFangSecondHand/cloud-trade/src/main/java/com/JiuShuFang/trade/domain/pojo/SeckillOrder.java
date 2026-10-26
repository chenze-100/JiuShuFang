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
 * 秒杀订单表
 * </p>
 *
 * @since 2026-10-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("seckill_order")
@Schema(description = "秒杀订单表")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "秒杀订单ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "秒杀活动ID")
    private Long seckillId;

    @Schema(description = "商品ID")
    private Long goodsId;

    @Schema(description = "抢购用户ID")
    private Long userId;

    @Schema(description = "书籍名称（快照）")
    private String bookName;

    @Schema(description = "成交价")
    private BigDecimal price;

    @Schema(description = "状态：1已抢到待付款")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
