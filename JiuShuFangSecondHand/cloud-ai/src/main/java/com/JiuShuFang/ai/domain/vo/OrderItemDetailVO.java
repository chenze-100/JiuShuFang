package com.JiuShuFang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 微服务拆分：该 VO 原属单体订单模块（现 cloud-trade），此处为 AI 服务侧的数据结构副本，
 * 后续接入 Feign 后由 cloud-trade 接口返回，可删除本副本。
 */
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
