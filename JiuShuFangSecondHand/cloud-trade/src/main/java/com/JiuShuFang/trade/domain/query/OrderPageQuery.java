package com.JiuShuFang.trade.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "订单分页查询条件")
public class OrderPageQuery extends PageQuery {

    @Schema(description = "查询条件,可以按照订单id查询,还能按照订单中商品名称查询")
    private String query;

    @Schema(description = "订单状态：1 待付款 2 待发货 3 待收货 4 已完成 5 已取消")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否是我卖出的")
    private Boolean isSeller;
}
