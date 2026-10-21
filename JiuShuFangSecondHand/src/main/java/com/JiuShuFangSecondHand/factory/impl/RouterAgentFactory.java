package com.JiuShuFangSecondHand.factory.impl;

import com.JiuShuFangSecondHand.agent.Agent;
import com.JiuShuFangSecondHand.agent.impl.BuyAgent;
import com.JiuShuFangSecondHand.agent.impl.NormalAgent;
import com.JiuShuFangSecondHand.agent.impl.RouterAgent;
import com.JiuShuFangSecondHand.agent.impl.SellAgent;
import com.JiuShuFangSecondHand.enums.ChatModelType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RouterAgentFactory {

    private final NormalAgent normalAgent;
    private final BuyAgent buyAgent;
    private final SellAgent sellAgent;
    private final RouterAgent routerAgent;


    private final Map<ChatModelType, Agent> agentMap = new EnumMap<>(ChatModelType.class);

    @PostConstruct
    private void init() {
        agentMap.put(ChatModelType.ROUTER, routerAgent);
        agentMap.put(ChatModelType.NORMAL, normalAgent);
        agentMap.put(ChatModelType.BUY, buyAgent);
        agentMap.put(ChatModelType.SELL, sellAgent);
    }

    /**
     * 获取对应的agent,如果没有对应的agent，则返回normalAgent
     * @param chatModelType
     * @return
     */
    public Agent getAgent(ChatModelType chatModelType) {
        return agentMap.getOrDefault(chatModelType , normalAgent);
    }
}
