package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单项 VO")
public class OrderItemVO {

    @Schema(description = "明细 ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long goodsId;

    @Schema(description = "书籍名称（快照）")
    private String bookName;

    @Schema(description = "封面图")
    private String bookImg;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "商品状态：1上架 2下架 3已售")
    private Integer goodStatus;

    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "是否评价")
    private Boolean isComment;

    @Schema(description = "小计")
    private BigDecimal totalPrice;
}
