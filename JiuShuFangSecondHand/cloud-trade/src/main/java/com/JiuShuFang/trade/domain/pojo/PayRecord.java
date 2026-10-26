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
 * 支付流水记录表
 * </p>
 *
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pay_record")
@Schema( description="支付流水记录表")
public class PayRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description  = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description  = "商户订单号")
    private Long orderNo;

    @Schema(description  = "支付宝订单号")
    private String outTradeNo;

    @Schema(description  = "交易状态 TRADE_SUCCESS-支付成功")
    private String tradeStatus;

    @Schema(description  = "支付金额")
    private BigDecimal totalAmount;

    @Schema(description  = "支付宝回调完整参数")
    private String notifyParams;

    private LocalDateTime createTime;


}
