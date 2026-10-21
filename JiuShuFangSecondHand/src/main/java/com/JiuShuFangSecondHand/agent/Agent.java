package com.JiuShuFangSecondHand.agent;

import com.JiuShuFangSecondHand.enums.ChatModelType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI 代理接口，提供与 AI 模型交互的核心功能
 */
public interface Agent {

    Object[] EMPTY_TOOLS = new Object[0];

    /**
     * 流式输出 AI 生成的内容
     * @param prompt 用户输入的提示词
     * @param conversationId 会话标识符，用于关联对话上下文
     * @return 包含 AI 生成内容的响应式流
     */
    Flux<String> contentWithFlux(String prompt, String conversationId);

    /**
     * 非流式输出 AI 生成的内容
     * @param prompt 用户输入的提示词
     * @param conversationId 会话标识符，用于关联对话上下文
     * @return AI 生成的完整内容字符串
     */
    String contentSimple(String prompt, String conversationId);


    ChatModelType getChatModelType();

    /**
     * 获取系统提示词
     * @return 处理后的提示词字符串
     */
    default String getSystemPrompt() {
        return "";
    }

    /**
     * 获取 AI 模型的顾问配置列表
     * @return 顾问配置列表，用于增强模型行为
     */
    default List<Advisor> getAdvisor() {
        return List.of();
    }

    /**
     * 获取 AI 模型名称
     * @return 模型名称字符串
     */
    default String getModel() {
        return "";
    }

    default Object[] tools(){
        return EMPTY_TOOLS;
    }


    default Map<String , Object> toolContext(String conversactionId , String requestId){
        return Map.of();
    }

    default Map<String , Object> adviorsParams(String conversactionId , String requestId){
        return Map.of();
    }


    ChatClient getchatClient();
}
