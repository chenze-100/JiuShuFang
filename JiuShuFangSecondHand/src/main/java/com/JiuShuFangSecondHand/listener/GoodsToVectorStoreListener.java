package com.JiuShuFangSecondHand.listener;

import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.JiuShuFangSecondHand.constants.MqConstant.Goods.*;

@Component  // Milvus 已停用：注释整个 MQ 监听器，不再消费向量增删改消息（恢复时取消注释）
@RequiredArgsConstructor
public class GoodsToVectorStoreListener {

    private final EmbeddingService embeddingService;


    /**
     * 保存商品到向量存储
     * @param text
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = GOODS_TO_SAVE_VS_QUEUE, durable = "true"),
            exchange = @Exchange(value = GOODS_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = {GOODS_TO_SAVE_VS_ROUTING_KEY}
    ))
    public void saveGoodsToVectorStore(String text) {
        Optional.ofNullable(text)
                .ifPresentOrElse(
                        g -> embeddingService
                                .saveGoodsInVectorStore(List.of(text)),
                        () -> {
                            throw new BusinessException("goods is null");
                        }
                );
    }


    /**
     * 删除商品到向量存储
     * @param goodsId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = GOODS_TO_DELETE_VS_QUEUE, durable = "true"),
            exchange = @Exchange(value = GOODS_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = {GOODS_TO_DELETE_VS_ROUTING_KEY}
    ))
    public void deleteGoodsToVectorStore(String goodsId) {
        Optional.ofNullable(goodsId)
                .ifPresentOrElse(
                        id -> embeddingService
                                .deleteGoodsFromVectorStore(goodsId),
                        () -> {
                            throw new BusinessException("goodsId is null");
                        }
                );
    }


    /**
     * 更新商品到向量存储
     * @param text
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = GOODS_TO_UPDATE_VS_QUEUE, durable = "true"),
            exchange = @Exchange(value = GOODS_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = {GOODS_TO_UPDATE_VS_ROUTING_KEY}
    ))
    public void updateGoodsToVectorStore(String text) {
        Optional.ofNullable(text)
                .ifPresentOrElse(
                        g -> embeddingService
                                .updateGoodsInVectorStore(text),
                        () -> {
                            throw new BusinessException("goods is null");
                        }
                );
    }
}
