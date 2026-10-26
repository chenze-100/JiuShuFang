package com.JiuShuFang.trade.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.JiuShuFang.trade.constants.MqConstant.Error.*;
import static com.JiuShuFang.trade.constants.MqConstant.Seckill.*;

@Configuration
public class MQMessageConverterConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    // ==================== 错误队列、交换机、绑定 ====================
    @Bean
    public Queue errorQueue() {
        return QueueBuilder.durable(ERROR_QUEUE).build();
    }

    @Bean
    public DirectExchange errorExchange() {
        return ExchangeBuilder.directExchange(ERROR_DIRECT_EXCHANGE)
                .durable(true).build();
    }

    @Bean
    public Binding errorBinding() {
        return BindingBuilder.bind(errorQueue())
                .to(errorExchange())
                .with(ERROR_ROUTING_KEY);
    }


    /**
     * 重试耗尽后，将消息转发到 error.direct -> error.queue
     */
    @Bean
    public MessageRecoverer republishMessageRecoverer(ConnectionFactory connectionFactory) {
        AmqpTemplate amqpTemplate = new RabbitTemplate(connectionFactory);
        return new RepublishMessageRecoverer(amqpTemplate, ERROR_DIRECT_EXCHANGE, ERROR_ROUTING_KEY);
    }


    // ==================== 秒杀队列、交换机、绑定 ====================
    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE).build();
    }

    @Bean
    public DirectExchange seckillExchange() {
        return ExchangeBuilder.directExchange(SECKILL_DIRECT_EXCHANGE)
                .durable(true).build();
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }

}
