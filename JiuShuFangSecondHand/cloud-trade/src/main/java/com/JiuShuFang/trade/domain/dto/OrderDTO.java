package com.JiuShuFang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "订单DTO")
public class OrderDTO {

    @Schema(description = "订单ID")
    private Long id;

    @NotNull(message = "订单总金额不能为空")
    @Schema(description = "订单总金额")
    private BigDecimal totalPrice;

    @NotNull(message = "收货地址ID不能为空")
    @Schema(description = "收货地址ID")
    private Long addressId;

    @Schema(description = "是否清空购物车")
    @NotNull(message = "是否清空购物车不能为空")
    private Boolean isClearCart;

    @Valid()
    @Schema(description = "订单项列表")
    private List<OrderItemDTO> orderItemDTOList;



}
