package com.JiuShuFang.trade.service;

import com.JiuShuFang.trade.domain.dto.OrderAIDTO;
import com.JiuShuFang.trade.domain.dto.OrderDTO;
import com.JiuShuFang.trade.domain.dto.OrderStatusUpdateDto;
import com.JiuShuFang.trade.domain.pojo.ChatMemory;
import com.JiuShuFang.trade.domain.query.OrderPageQuery;
import com.JiuShuFang.trade.domain.pojo.Order;
import com.JiuShuFang.trade.domain.vo.OrderDetailVO;
import com.JiuShuFang.trade.domain.vo.OrderVO;
import com.JiuShuFang.common.util.PageDTO;
import com.JiuShuFang.common.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface IOrderService extends IService<Order> {

    Result<Long> createOrder(OrderDTO dto);

    Result<PageDTO<OrderVO>> pageOrders(OrderPageQuery dto);

    Result<Long> countUnpaidOrders();

    Result<Void> deleteOrder(Long orderId);

    Result<Void> updateOrderStatus(OrderStatusUpdateDto dto);

    Result<OrderDetailVO> getOrderDetail(Long orderId);

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    void updateStatus(Order order, OrderStatusUpdateDto dto);

    Result<Void> updateAddress(Long orderId, Long addressId);

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    void setDefaultAddressFromAI(OrderAIDTO orderAIDTO);

    @Transactional(rollbackFor = Exception.class)
    void updateAIOrderInfo(String conversationId, Order order);

    @Transactional(rollbackFor = Exception.class)
    void updateAIOrderInfo(ChatMemory chatMemory, Order order);

    @Transactional(rollbackFor = Exception.class)
    void updateAIOrderInfo(String conversationId, OrderDetailVO orderDetailVO);
}
