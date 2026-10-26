package com.JiuShuFang.ai.agent.impl;

import com.JiuShuFang.ai.agent.abstractAgent.AbstarctAgent;
import com.JiuShuFang.ai.constants.ChatClientSkills;
import com.JiuShuFang.ai.enums.ChatModelType;
import com.JiuShuFang.ai.tool.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SellAgent extends AbstarctAgent {
    private final ToolService toolService;
    private final ChatMemory redisChatMemory;
    private final ChatMemory redisRouterChatMemory;


    @Override
    public ChatModelType getChatModelType() {
        return ChatModelType.SELL;
    }

    @Override
    public String getSystemPrompt() {
        return ChatClientSkills.SELL_PROMPT;
    }


    @Override
    public Object[] tools() {
        return new Object[]{
                toolService
        };
    }


    @Override
    public Map<String, Object> adviorsParams(String conversationId, String requestId) {
        return Map.of(ChatMemory.CONVERSATION_ID, conversationId);
    }

    @Override
    public List<Advisor> getAdvisor() {
        // 顺序很重要：router 记忆在前（含用户原始提问），主记忆在后。
        // 若路由回复"BUY/SELL"被意外写进 router 记忆，主记忆排在最后
        // 可确保模型"最近读到的"是正常对话而非路由码，防止模型模仿回复 BUY/SELL
        return List.of(
                MessageChatMemoryAdvisor.builder(redisRouterChatMemory).build(),
                MessageChatMemoryAdvisor.builder(redisChatMemory).build()
        );
    }
}
