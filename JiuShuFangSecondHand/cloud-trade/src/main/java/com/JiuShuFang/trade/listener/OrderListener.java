package com.JiuShuFang.trade.listener;


import com.JiuShuFang.trade.domain.dto.CommentDto;
import com.JiuShuFang.trade.domain.dto.OrderStatusUpdateDto;
import com.JiuShuFang.trade.domain.pojo.Order;
import com.JiuShuFang.trade.domain.pojo.OrderItem;
import com.JiuShuFang.trade.domain.pojo.PayOrder;
import com.JiuShuFang.trade.domain.pojo.PayRecord;
import com.JiuShuFang.trade.mapper.OrderItemMapper;
import com.JiuShuFang.trade.service.IOrderService;
import com.JiuShuFang.trade.service.IPayOrderService;
import com.JiuShuFang.trade.service.IPayRecordService;
import com.JiuShuFang.trade.service.IPayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.JiuShuFang.trade.constants.MqConstant.Order.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderListener {

    private final IOrderService orderService;
    private final IPayOrderService payOrderService;
    private final IPayRecordService payRecordService;
//    private final IPayService payService;
    private final OrderItemMapper orderItemMapper;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = ORDER_TO_PAY_QUEUE, durable = "true"),
            exchange = @Exchange(value = ORDER_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = {ORDER_TO_PAY_ROUTING_KEY}
    ))
//    public void enablePaymentFromOrderSend(Long orderId) {
//        //查看订单是否已经支付
//        Order order = orderService.getById(orderId);
//        if (order.getStatus() != 1) {
//            //如果不是代付款说明已支付
//            log.debug("订单已支付:{}", orderId);
//            return;
//        }
//        //如果订单显示没有支付,查看支付流水是否支付
//        PayOrder payOrder = payOrderService.lambdaQuery()
//                .eq(PayOrder::getOrderNo, orderId)
//                .one();
//
//        if (payOrder == null) {                     // ← ✅ 加空判断
//            log.warn("支付流水不存在，orderId={}", orderId);
//            // 直接跳到后面查支付宝回调的逻辑
//        } else {
//            Integer status = payOrder.getStatus();
//            if (status.equals(1)) {
//                order.setStatus(2);
//                orderService.updateById(order);
//                return;
//            }
//        }
//        //如果二者都没有显示已支付,查看支付宝回调记录有没有支付
//        PayRecord payRecord = payRecordService.lambdaQuery()
//                .eq(PayRecord::getOrderNo, orderId)
//                .one();
//        if (payRecord != null
//                && payRecord.getTradeStatus().equals("TRADE_SUCCESS")) {
//            //如果支付宝回调记录显示已经支付了,修改订单状态和支付流水
//            payService.setOrderAndPayStatusToSuccess(orderId.toString());
//            return;
//        }
//        //如果都显示未支付,修改订单为已取消,回复库存
//        OrderStatusUpdateDto orderStatusUpdateDto = new OrderStatusUpdateDto();
//        orderStatusUpdateDto.setOrderId(orderId);
//        orderStatusUpdateDto.setStatus(5);
//        orderStatusUpdateDto.setCancelReason("超时自动取消");
//        orderStatusUpdateDto.setUserId(order.getUserId());
//        orderService.updateOrderStatus(orderStatusUpdateDto);
//    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = ORDER_STATUS_QUEUE, durable = "true"),
            exchange = @Exchange(value = ORDER_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key ={ORDER_STATUS_ROUTING_KEY}
    ))
    public void setOrderStatus(CommentDto dto) {
        if (dto == null) {
            log.debug("订单状态更新参数为空");
            return;
        }
        OrderItem orderItem = orderItemMapper.selectOne(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, dto.getOrderId())
                        .eq(OrderItem::getGoodsId, dto.getGoodsId())
        );
        if (orderItem == null) {
            log.debug("订单状态更新参数为空");
            return;
        }
        OrderStatusUpdateDto orderStatusUpdateDto = new OrderStatusUpdateDto();
        orderStatusUpdateDto.setOrderId(dto.getOrderId());
        orderStatusUpdateDto.setStatus(6);
        orderStatusUpdateDto.setOrderItemId(orderItem.getId());
        orderStatusUpdateDto.setUserId(dto.getUserId());
        orderService.updateOrderStatus(orderStatusUpdateDto);
    }
}
