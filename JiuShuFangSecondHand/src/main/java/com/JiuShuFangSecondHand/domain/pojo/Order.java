package com.JiuShuFangSecondHand.domain.pojo;

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
 * 订单表
 * </p>
 *
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`order`")
@Schema(description="订单表")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    @Schema(description = "用户ID（买家）")
    private Long userId;

    @Schema(description = "订单总金额")
    private BigDecimal totalPrice;

    @Schema(description = "地址id")
    private Long addressId;


    @Schema(description = "收件人")
    private String receiverName;

    @Schema(description = "收件电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "订单状态：1待付款 2待发货 3待收货 4已完成 5已取消")
    private Integer status;

    @Schema(description = "支付方式：1支付宝 2余额")
    private Integer payType;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "确认收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "评价时间")
    private LocalDateTime commentTime;

    @Schema(description = "当前评价数量")
    private Integer currentCommentNum;

    @Schema(description = "最大评价数量")
    private Integer maxCommentNum;

    @Schema(description = "是否删除：0未删除 1已删除")
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
