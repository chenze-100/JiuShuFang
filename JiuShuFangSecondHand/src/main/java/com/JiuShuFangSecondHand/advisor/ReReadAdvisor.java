package com.JiuShuFangSecondHand.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReReadAdvisor implements BaseAdvisor {

    public static final String RE_READ_ADVISOR = "{promotion} , pleace read the promotion again : {promotion}";
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Prompt originalPrompt = chatClientRequest.prompt();
        List<UserMessage> originalMessages = originalPrompt.getUserMessages();

        // 构建新消息列表
        List<Message> newMessages = new ArrayList<>();
        for (UserMessage msg : originalMessages) {
            // 只修改用户消息
            if (msg != null) {
                // 包装文本
                String newText = PromptTemplate.builder()
                        .template(RE_READ_ADVISOR)
                        .build()
                        .render(Map.of("promotion", msg.getText()));
                // 保留图片、媒体、元数据，只替换文字
                UserMessage userMsg = msg.mutate()
                        .text(newText)
                        .build();
                newMessages.add(userMsg);
            }
        }
        ChatClientRequest clientRequest = chatClientRequest.mutate()
                .prompt(Prompt.builder().messages(newMessages).build())
                .build();
        return clientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
