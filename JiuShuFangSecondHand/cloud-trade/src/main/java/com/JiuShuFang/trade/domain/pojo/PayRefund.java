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
 * 退款表
 * </p>
 *
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_refund")
@Schema(description = "退款表")
public class PayRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号")
    private Long orderNo;

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "0-申请中 1-成功 2-失败")
    private Integer status;

    private LocalDateTime createTime;


}
