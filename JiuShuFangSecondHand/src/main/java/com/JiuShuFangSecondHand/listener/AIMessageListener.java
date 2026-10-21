package com.JiuShuFangSecondHand.listener;


import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.domain.pojo.ChatMemory;
import com.JiuShuFangSecondHand.domain.vo.OrderDetailVO;
import com.JiuShuFangSecondHand.service.IChatMemoryService;
import com.JiuShuFangSecondHand.util.AIJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.JiuShuFangSecondHand.constants.MqConstant.AiMessage.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AIMessageListener {

    private final IChatMemoryService chatMemoryService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = AI_MESSAGE_QUEUE, durable = "true"),
            exchange = @Exchange(value = AI_MESSAGE_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = AI_MESSAGE_ROUTING_KEY
    ))
    public void setMessageToDB(Map<String, Object> map) {
        Assert.notNull(map, "map is null");
        var conversationId = map.get("conversationId");
        Assert.notNull(conversationId, "conversationId is null");
        var userId = map.get("userId");
        Assert.notNull(userId, "userId is null");
        Map<String, Object> messageMap = JSONUtil.parseObj(map.get("messageJson"));
        var messageType = messageMap.get("messageType");
        Assert.notNull(messageType, "messageType is null");
        var textContent = messageMap.get("textContent");
        Assert.notNull(textContent, "textContent is null");
        ChatMemory chatMessage = new ChatMemory()
                .setConversationId(conversationId.toString())
                .setUserId(Long.valueOf(userId.toString()))
                .setType(messageType.toString())
                .setContent(textContent.toString());
        if (!messageType.equals("ASSISTANT")) {
            chatMemoryService.save(chatMessage);
            return;
        }
        OrderDetailVO orderDetailVO = AIJsonUtil
                .extractJson(messageMap.get("textContent").toString()
                        , OrderDetailVO.class);
        if (orderDetailVO != null && orderDetailVO.getId() != null) {
            chatMessage.setOrderId(orderDetailVO.getId());
        }
        chatMemoryService.save(chatMessage);
    }
}
