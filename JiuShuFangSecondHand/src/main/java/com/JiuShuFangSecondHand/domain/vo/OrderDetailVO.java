package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单详情 VO")
public class OrderDetailVO {

    @Schema(description = "订单 ID")
    private Long id;

    @Schema(description = "订单状态：1 待付款 2 待发货 3 待收货 4 已完成 5 已取消")
    private Integer status;

    @Schema(description = "订单总价")
    private BigDecimal totalPrice;

    @Schema(description = "支付方式：1 支付宝 2 余额")
    private Integer payType;

    @Schema(description = "地址id")
    private Long addressId;

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "运费金额")
    private BigDecimal freightAmount;

    @Schema(description = "优惠券抵扣金额")
    private BigDecimal couponAmount;

    @Schema(description = "订单商品列表")
    private List<OrderItemDetailVO> orderItemList;
}
