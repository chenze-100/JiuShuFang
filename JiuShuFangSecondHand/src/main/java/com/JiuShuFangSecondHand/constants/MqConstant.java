package com.JiuShuFangSecondHand.constants;

public interface MqConstant {

    interface Error {
        // 错误队列配置
        String ERROR_DIRECT_EXCHANGE = "error.direct";
        String ERROR_QUEUE = "error.queue";
        String ERROR_ROUTING_KEY = "error";
    }


    interface Order {
        // 订单队列配置
        String ORDER_DIRECT_EXCHANGE = "order.direct";
        String ORDER_QUEUE = "order.queue";
        //处理超时订单
        String ORDER_TO_PAY_QUEUE = "order.to.pay.queue";
        String ORDER_TO_PAY_ROUTING_KEY = "order.to.pay";
        //处理订单状态
        String ORDER_STATUS_QUEUE = "order.status.queue";
        String ORDER_STATUS_ROUTING_KEY = "order.status";

    }


    interface Comment {
        // 评论队列配置
        String COMMENT_DIRECT_EXCHANGE = "comment.direct";
        String COMMENT_QUEUE = "comment.queue";
        String COMMENT_ROUTING_KEY = "comment";
    }

    interface AiMessage {
        // AI 消息存储配置
        String AI_MESSAGE_DIRECT_EXCHANGE = "ai.message.direct";
        String AI_MESSAGE_QUEUE = "ai.message.queue";
        String AI_MESSAGE_ROUTING_KEY = "ai.message";
    }

    interface AiList {
        // AI 消息列表存储配置
        String AI_LIST_DIRECT_EXCHANGE = "ai.list.direct";
        String AI_LIST_QUEUE = "ai.list.queue";
        String AI_LIST_ROUTING_KEY = "ai.list";
    }

    interface Goods {
        // 商品队列配置
        String GOODS_DIRECT_EXCHANGE = "goods.direct";
        // 保存商品到向量数据库队列
        String GOODS_TO_SAVE_VS_QUEUE = "goods.to.save.vs.queue";
        String GOODS_TO_SAVE_VS_ROUTING_KEY = "goods.to.save.vs";
        // 保存商品到向量数据库队列
        String GOODS_TO_DELETE_VS_QUEUE = "goods.to.delete.vs.queue";
        String GOODS_TO_DELETE_VS_ROUTING_KEY = "goods.to.delete.save.vs";
        // 保存商品到向量数据库队列
        String GOODS_TO_UPDATE_VS_QUEUE = "goods.to.update.vs.queue";
        String GOODS_TO_UPDATE_VS_ROUTING_KEY = "goods.to.update.save.vs";
    }

    interface Seckill {
        // 秒杀下单队列配置（Redis 预减通过后投递，异步落库）
        String SECKILL_DIRECT_EXCHANGE = "seckill.direct";
        String SECKILL_QUEUE = "seckill.queue";
        String SECKILL_ROUTING_KEY = "seckill";
    }

}
