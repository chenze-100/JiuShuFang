package com.JiuShuFang.ai.domain.tool;

import com.JiuShuFang.ai.domain.vo.OrderItemDetailVO;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ToolOrderVO {


    @ToolParam(description = "订单 ID")
    private Long id;

    @ToolParam(description = "订单状态：1 待付款 2 待发货 3 待收货 4 已完成 5 已取消")
    private Integer status;

    @ToolParam(description = "订单总价")
    private BigDecimal totalPrice;

    @ToolParam(description = "支付方式：1 支付宝 2 余额")
    private Integer payType;

    @ToolParam(description = "收货人姓名")
    private String receiverName;

    @ToolParam(description = "收货人电话")
    private String receiverPhone;

    @ToolParam(description = "收货地址")
    private String receiverAddress;

    @ToolParam(description = "下单时间")
    private LocalDateTime createTime;

    @ToolParam(description = "支付时间")
    private LocalDateTime payTime;

    @ToolParam(description = "订单商品列表")
    private List<OrderItemDetailVO> orderItemList;


}
