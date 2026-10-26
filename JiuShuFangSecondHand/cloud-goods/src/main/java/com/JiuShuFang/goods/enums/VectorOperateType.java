package com.JiuShuFang.goods.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.JiuShuFang.goods.constants.MqConstant.Goods.*;

@Getter
@AllArgsConstructor
public enum VectorOperateType {
    SAVE() {
        @Override
        public void sendMessageToMq(String text, RabbitTemplate rabbitTemplate) {
            rabbitTemplate.convertAndSend(
                    GOODS_DIRECT_EXCHANGE,
                    GOODS_TO_SAVE_VS_ROUTING_KEY,
                    text
            );
        }
    },
    DELETE() {
        @Override
        public void sendMessageToMq(String text, RabbitTemplate rabbitTemplate) {
            rabbitTemplate.convertAndSend(
                    GOODS_DIRECT_EXCHANGE,
                    GOODS_TO_DELETE_VS_ROUTING_KEY,
                    text
            );
        }
    },
    UPDATE() {
        @Override
        public void sendMessageToMq(String text, RabbitTemplate rabbitTemplate) {
            rabbitTemplate.convertAndSend(
                    GOODS_DIRECT_EXCHANGE,
                    GOODS_TO_UPDATE_VS_ROUTING_KEY,
                    text
            );
        }
    };

    public abstract void sendMessageToMq(String text, RabbitTemplate rabbitTemplate);


}
