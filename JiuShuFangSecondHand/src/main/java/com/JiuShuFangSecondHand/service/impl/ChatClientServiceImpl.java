package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.util.StrUtil;
import com.JiuShuFangSecondHand.agent.Agent;
import com.JiuShuFangSecondHand.agent.impl.RouterAgent;
import com.JiuShuFangSecondHand.domain.pojo.ChatList;
import com.JiuShuFangSecondHand.enums.ChatModelType;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.factory.impl.RouterAgentFactory;
import com.JiuShuFangSecondHand.service.IChatClientService;
import com.JiuShuFangSecondHand.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.JiuShuFangSecondHand.constants.MqConstant.AiList.AI_LIST_DIRECT_EXCHANGE;
import static com.JiuShuFangSecondHand.constants.MqConstant.AiList.AI_LIST_ROUTING_KEY;


@Service
@RequiredArgsConstructor
public class ChatClientServiceImpl implements IChatClientService {
    private final RouterAgentFactory routerAgentFactory;
    private final RabbitTemplate rabbitTemplate;
    private final RouterAgent routerAgent;

    @Override
    public String chat(String prompt, String conversationId, String imageUrl) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");

        }
        String key = userId + ":" + conversationId;
        String newprompt = StrUtil.isNotBlank(imageUrl)
                ? prompt + ",imageUrl : " + imageUrl
                : prompt;
        String content = routerAgent.contentSimple(newprompt, key);
        // 路由结果解析：先按精确 code 匹配，失败再用关键词模糊匹配，最终兜底 NORMAL
        // 避免路由模型输出不规范（如"BUY。"或整句话）时直接抛异常，导致本次对话空白
        ChatModelType chatModelType = ChatModelType.fromCode(content);
        if (chatModelType == null) {
            chatModelType = ChatModelType.fromContent(content);
        }
        if (chatModelType == null) {
            chatModelType = ChatModelType.NORMAL;
        }
        Agent agent = routerAgentFactory.getAgent(chatModelType);
        String retContent = agent.contentSimple(newprompt, key);
        saveConList(prompt, conversationId, userId);
        return retContent;

    }

    private void saveConList(String conName, String conversationId, Long userId) {
        ChatList chatList = new ChatList();
        chatList.setConversationId(conversationId);
        chatList.setUserId(userId);
        chatList.setConversationName(conName);
        chatList.setCreateTime(LocalDateTime.now());
        rabbitTemplate.convertAndSend(AI_LIST_DIRECT_EXCHANGE,
                AI_LIST_ROUTING_KEY,
                chatList);
    }
}

