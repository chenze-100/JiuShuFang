package com.JiuShuFangSecondHand.domain.tool;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;

@Data
public class ToolOrderDetailVO {

    @ToolParam(description = "商品 ID")
    private Long goodsId;

    @ToolParam(description = "商品名称")
    private String goodsName;

    @ToolParam(description = "商品图片路径")
    private String goodsImage;

    @ToolParam(description = "商品规格")
    private String goodsSpec;

    @ToolParam(description = "购买数量")
    private Integer num;

    @ToolParam(description = "商品总价")
    private BigDecimal price;

    @ToolParam(description = "商品状态：1 在售 2 下架 3 已售")
    private Integer goodStatus;
}
