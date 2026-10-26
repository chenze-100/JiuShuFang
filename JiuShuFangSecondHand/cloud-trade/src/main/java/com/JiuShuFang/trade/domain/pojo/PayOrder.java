package com.JiuShuFang.trade.domain.pojo;

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
 * 支付订单表
 * </p>
 *
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_order")
@Schema( description="支付订单表")
public class PayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description  = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description  = "商户订单号（唯一）")
    private Long orderNo;

    @Schema(description  = "支付宝/微信订单号")
    private Long outTradeNo;

    @Schema(description  = "用户ID")
    private Long userId;


    @Schema(description  = "订单金额")
    private BigDecimal totalAmount;


    @Schema(description  = "订单状态 0-待支付 1-已支付 2-已关闭 3-已退款")
    private Integer status;

    @Schema(description  = "支付类型 1-支付宝 2-微信")
    private Integer payType;

    @Schema(description  = "支付时间")
    private LocalDateTime payTime;

    @Schema(description  = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description  = "创建时间")
    private LocalDateTime createTime;

    @Schema(description  = "更新时间")
    private LocalDateTime updateTime;


}
