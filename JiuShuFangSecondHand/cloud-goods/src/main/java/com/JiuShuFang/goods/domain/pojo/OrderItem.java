package com.JiuShuFang.goods.domain.pojo;

import java.math.BigDecimal;
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
 * 订单明细表
 * </p>
 *
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
@Schema(description="订单明细表")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "卖家id")
    private Long sellerId;

    @Schema(description = "商品ID")
    private Long goodsId;

    @Schema(description = "书籍名称（快照）")
    private String bookName;

    @Schema(description = "封面图")
    private String bookImg;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "是否已经评价")
    private Boolean isComment;

    @Schema(description = "小计")
    private BigDecimal totalPrice;

    private Integer isDeleted;

    private LocalDateTime createTime;


}
