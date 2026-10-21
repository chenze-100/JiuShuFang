package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.OrderAIDTO;
import com.JiuShuFangSecondHand.domain.dto.OrderDTO;
import com.JiuShuFangSecondHand.domain.dto.OrderStatusUpdateDto;
import com.JiuShuFangSecondHand.domain.pojo.ChatMemory;
import com.JiuShuFangSecondHand.domain.query.OrderPageQuery;
import com.JiuShuFangSecondHand.domain.pojo.Order;
import com.JiuShuFangSecondHand.domain.vo.OrderDetailVO;
import com.JiuShuFangSecondHand.domain.vo.OrderVO;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
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
