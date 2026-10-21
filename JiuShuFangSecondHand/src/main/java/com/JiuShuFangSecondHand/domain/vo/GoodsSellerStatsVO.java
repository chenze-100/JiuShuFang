package com.JiuShuFangSecondHand.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GoodsSellerStatsVO {
    private int totalProducts; // 商品数量
    private int totalSales; // 总销量
    private int totalOrders; // 订单数量
}
