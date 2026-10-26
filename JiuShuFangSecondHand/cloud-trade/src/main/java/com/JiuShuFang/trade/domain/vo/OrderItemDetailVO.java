package com.JiuShuFang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单商品详情 VO")
public class OrderItemDetailVO {

    @Schema(description = "商品 ID")
    private Long goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片路径")
    private String goodsImage;

    @Schema(description = "商品规格")
    private String goodsSpec;

    @Schema(description = "购买数量")
    private Integer num;

    @Schema(description = "商品总价")
    private BigDecimal price;

    @Schema(description = "商品状态：1 在售 2 下架 3 已售")
    private Integer goodStatus;
}
