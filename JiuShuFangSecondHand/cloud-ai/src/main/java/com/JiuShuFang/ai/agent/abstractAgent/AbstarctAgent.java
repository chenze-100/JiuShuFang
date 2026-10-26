package com.JiuShuFang.ai.agent.abstractAgent;


import cn.hutool.core.util.IdUtil;
import com.JiuShuFang.ai.agent.Agent;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;


public abstract class AbstarctAgent implements Agent {


    @Autowired
    private ChatClient chatClient;


    @Override
    public Flux<String> contentWithFlux(String prompt, String conversationId) {
        String requestId = getRequestId();
        return this.chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(prompt)
                .advisors(a -> a.advisors(this.getAdvisor())
                        .params(this.adviorsParams(conversationId, requestId)))
                .tools(this.tools())
                .toolContext(this.toolContext(conversationId, requestId))
                .stream()
//                .chatResponse()
                .content();
    }

    @Override
    public String contentSimple(String prompt, String conversationId) {
        String requestId = getRequestId();
        return this.getchatClient().prompt()
                .system(this.getSystemPrompt())
                .user(prompt)
                .advisors(a -> a.advisors(this.getAdvisor())
                        .params(this.adviorsParams(conversationId, requestId)))
                .tools(this.tools())
                .toolContext(this.toolContext(conversationId, requestId))
                .call()
                .content();
    }

    @Override
    public ChatClient getchatClient() {
        return chatClient;
    }

    private static String getRequestId() {
        return IdUtil.fastSimpleUUID();
    }


   /* @Override
    public Map<String, Object> adviorsParams(String conversationId, String requestId) {
        return Map.of(ChatMemory.CONVERSATION_ID, conversationId);
    }*/

}
