package com.JiuShuFang.trade.service;

import com.JiuShuFang.trade.domain.pojo.Order;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface IPayService {
    void pcPay(HttpServletResponse response, Long orderId);

    @Transactional(rollbackFor = Exception.class)
    boolean checkStock(Order order);

    String notify(HttpServletRequest request);

    @Transactional(rollbackFor = Exception.class)
    void setOrderAndPayStatusToSuccess(String outTradeNo);

    /**
     * 测试阶段模拟支付：不对接真实支付渠道，
     * 直接完成"校验订单 -> 生成支付单 -> 更新订单/支付单状态 -> 记录支付流水"
     */
    @Transactional(rollbackFor = Exception.class)
    void balancePay(Long orderId, BigDecimal amount);
}
