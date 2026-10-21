package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "订单 VO")
public class OrderVO {

    @Schema(description = "订单 ID")
    private Long id;

    @Schema(description = "订单总金额")
    private BigDecimal totalPrice;

    @Schema(description = "地址id")
    private Long addressId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "收件人")
    private String receiverName;

    @Schema(description = "支付方式：1 支付宝 2 余额")
    private Integer payType;

    @Schema(description = "收件电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "订单状态：1 待付款 2 待发货 3 待收货 4 已完成 5 已取消")
    private Integer status;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "订单项列表")
    private List<OrderItemVO> orderItemVOList;
}
