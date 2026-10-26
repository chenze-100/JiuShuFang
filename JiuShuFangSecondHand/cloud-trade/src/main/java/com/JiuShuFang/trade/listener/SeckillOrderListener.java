package com.JiuShuFang.trade.listener;

import com.JiuShuFang.trade.domain.dto.SeckillOrderMessage;
import com.JiuShuFang.trade.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.JiuShuFang.trade.constants.MqConstant.Seckill.*;

/**
 * <p>
 * 秒杀下单消费者：异步落库 + 失败补偿
 * </p>
 * <p>
 * 必须两层结构：@Transactional 边界在 Service 方法上，
 * 监听器只负责 try/catch 并调用补偿逻辑。
 * 若把 @Transactional 加在本方法上又在此处 catch，
 * 异常不会穿过 Spring 代理 → 事务不会回滚，插入的秒杀订单会残留，
 * 直接污染"一人一单"的唯一索引。
 * </p>
 * <p>
 * 失败也 ack（不抛异常出去）：项目未开启 listener retry，
 * 抛异常会按默认 requeue=true 无限重投形成死循环。
 * </p>
 *
 * @since 2026-10-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeckillOrderListener {

    private final ISeckillService seckillService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = SECKILL_QUEUE, durable = "true"),
            exchange = @Exchange(value = SECKILL_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = {SECKILL_ROUTING_KEY}
    ))
    public void onSeckillOrder(SeckillOrderMessage msg) {
        if (msg == null || msg.getSeckillId() == null || msg.getUserId() == null) {
            log.warn("秒杀消息内容非法，直接丢弃: {}", msg);
            return;
        }
        try {
            seckillService.handleSeckillOrder(msg);
        } catch (Exception e) {
            log.error("秒杀落库失败，执行 Redis 回补 seckillId={} userId={}",
                    msg.getSeckillId(), msg.getUserId(), e);
            seckillService.compensate(msg);
        }
    }
}
