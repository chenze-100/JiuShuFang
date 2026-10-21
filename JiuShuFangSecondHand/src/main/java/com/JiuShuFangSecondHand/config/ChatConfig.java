package com.JiuShuFangSecondHand.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.JiuShuFangSecondHand.advisor.VectorPreHandleAdvisor;
import com.JiuShuFangSecondHand.constants.ChatClientSkills;
import com.JiuShuFangSecondHand.repository.MyLettuceRedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;


@Configuration
public class ChatConfig {

    @Bean
    public ChatClient chatClient(
            DashScopeChatModel dashScopeChatModel) {
        // 注意1：这里不能挂 MessageChatMemoryAdvisor！
        // 每个 Agent（Router/Buy/Sell/Normal）都会通过 getAdvisor() 追加自己的记忆 Advisor，
        // 如果这里再挂一份主记忆 Advisor，会导致：
        // 1. RouterAgent 的路由结果（BUY/SELL/NORMAL）被写进主记忆 → 泄漏到聊天记录
        // 2. Normal/SellAgent 的用户消息和回复被写入两次（默认 Advisor + 请求 Advisor 各写一次）
        //
        // 注意2：这里也不能 defaultTools(toolService)！
        // BuyAgent/SellAgent 的 tools() 方法已经注册了 toolService，若默认再注册一次，
        // Spring AI 会校验出重名工具（queryBook、listBook 等）直接抛异常
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(ChatClientSkills.CHAT_CLIENT_SKILLS_PROMPT)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .model("qwen3.8-27b")
                                .temperature(0.3)
                                .maxToken(1024)
                                .multiModel(true)
                                .build()
                )
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new SafeGuardAdvisor(List.of("违禁词示例") , "你不能讨论与该违禁词有关的任何事情" , 0)
                )
                .build();
    }

        @Bean
    public ChatClient vectorChatClient(
            DashScopeChatModel dashScopeChatModel,
            ChatMemory redisVectorChatMemory
            , VectorStore vectorStore) {  // Milvus 已停用：移除 VectorStore 参数
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem("You are a helpful assistant.请用温柔的语气和用户对话")
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .model("qwen3.8-27b")
                                .temperature(1.4)
                                .maxToken(1024)
                                .multiModel(true)
                                .build()
                )
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisVectorChatMemory).build()
//                        QuestionAnswerAdvisor
                )
                .build();
    }

    /**
     * 自定义redis存储聊天记录
     * @param redisTemplate
     * @return
     */

    @Bean
    public MyLettuceRedisChatMemoryRepository redisChatMemoryRepository(
            RedisTemplate<String, String> redisTemplate,
            RabbitTemplate rabbitTemplate
    ) {
        return new MyLettuceRedisChatMemoryRepository(redisTemplate, "chat:memory:", rabbitTemplate);
    }



    /**
     * 自定义redis存储路由聊天记录
     * @param redisTemplate
     * @return
     */

    @Bean
    public MyLettuceRedisChatMemoryRepository redisChatRouterMemoryRepository(
            RedisTemplate<String, String> redisTemplate,
            RabbitTemplate rabbitTemplate
    ) {
        return new MyLettuceRedisChatMemoryRepository(redisTemplate, "chat:router:", rabbitTemplate);
    }

    @Bean
    public MyLettuceRedisChatMemoryRepository redisVectorChatMemoryRepository(
            RedisTemplate<String, String> redisTemplate,
            RabbitTemplate rabbitTemplate
    ) {
        return new MyLettuceRedisChatMemoryRepository(redisTemplate, "chat:vector:" , rabbitTemplate);
    }


    /**
     * redis存储聊天记录
     * @param redisChatMemoryRepository
     * @return
     */
    @Bean
    public MessageWindowChatMemory redisChatMemory(MyLettuceRedisChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(redisChatMemoryRepository)
                .build();
    }

    /**
     * redis存储聊天记录
     * @param redisChatRouterMemoryRepository
     * @return
     */
    @Bean
    public MessageWindowChatMemory redisRouterChatMemory(MyLettuceRedisChatMemoryRepository redisChatRouterMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(redisChatRouterMemoryRepository)
                .build();
    }

    @Bean
    public MessageWindowChatMemory redisVectorChatMemory(MyLettuceRedisChatMemoryRepository redisVectorChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(redisVectorChatMemoryRepository)
                .build();
    }

    /**
     * springai自带分割文本
     * @return
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMinChunkSizeChars(80)
                .build();
    }




    /**
     * 向量检索前置处理(自定义拦截)
     * @return
     */
    @Bean
    public VectorPreHandleAdvisor vectorPreHandleAdvisor() {
        return new VectorPreHandleAdvisor();
    }
}
