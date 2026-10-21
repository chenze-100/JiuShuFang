package com.JiuShuFangSecondHand.listener;


import com.JiuShuFangSecondHand.domain.pojo.ChatList;
import com.JiuShuFangSecondHand.service.IChatListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.JiuShuFangSecondHand.constants.MqConstant.AiList.*;


@RequiredArgsConstructor
@Component
@Slf4j
public class AIChatListListener {

    private final IChatListService chatListService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = AI_LIST_QUEUE, durable = "true"),
            exchange = @Exchange(value = AI_LIST_DIRECT_EXCHANGE, durable = "true", type = "direct"),
            key = AI_LIST_ROUTING_KEY
    ))
    public void setMessageToDB(ChatList chatList) {
        if(chatList == null){
            log.error("chatList is null");
            return;
        }
        ChatList list = chatListService.getById(chatList.getConversationId());
        if(list != null){
            return;
        }
        chatListService.save(chatList);
    }
}
