package com.JiuShuFang.ai.agent.impl;

import com.JiuShuFang.ai.agent.abstractAgent.AbstarctAgent;
import com.JiuShuFang.ai.constants.ChatClientSkills;
import com.JiuShuFang.ai.enums.ChatModelType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RouterAgent extends AbstarctAgent {

    private final ChatMemory redisRouterChatMemory;

    @Override
    public ChatModelType getChatModelType() {
        return ChatModelType.ROUTER;
    }

    @Override
    public String getSystemPrompt() {
        return ChatClientSkills.ROUTER_PROMPT;
    }

    @Override
    public Map<String, Object> adviorsParams(String conversationId, String requestId) {
        return Map.of(ChatMemory.CONVERSATION_ID, conversationId);
    }

    @Override
    public List<Advisor> getAdvisor() {
        // 只用 router 记忆：路由的用户消息和判断结果都会累积在 chat:router: 中，
        // 形成路由视角的上下文（"用户先问分类→AI引导→用户回java"），
        // 足以支撑"承接上文的短回复"判断。
        // 注意：绝不能挂主记忆 Advisor，否则路由回复 "BUY/SELL" 会被写进主记忆，
        // 重新引发路由码泄漏到聊天记录的问题。
        return List.of(
                MessageChatMemoryAdvisor.builder(redisRouterChatMemory).build()
        );
    }
}
