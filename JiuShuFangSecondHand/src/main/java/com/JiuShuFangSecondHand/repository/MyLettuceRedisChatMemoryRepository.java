package com.JiuShuFangSecondHand.repository;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.memory.redis.BaseRedisChatMemoryRepository;
import com.JiuShuFangSecondHand.enums.ChatModelType;
import org.springframework.ai.chat.messages.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.JiuShuFangSecondHand.constants.MqConstant.AiMessage.AI_MESSAGE_DIRECT_EXCHANGE;
import static com.JiuShuFangSecondHand.constants.MqConstant.AiMessage.AI_MESSAGE_ROUTING_KEY;

public class MyLettuceRedisChatMemoryRepository extends BaseRedisChatMemoryRepository {

    public static final String KEY_PREFIX = "chat:memory:";

    public final RedisTemplate<String, String> redisTemplate;

    public final RabbitTemplate rabbitTemplate;


    public final String keyPrefix;

    public MyLettuceRedisChatMemoryRepository(RedisTemplate<String, String> redisTemplate, String keyPrefix, RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.rabbitTemplate = rabbitTemplate;
    }


    public MyLettuceRedisChatMemoryRepository(RedisTemplate<String, String> redisTemplate, RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = KEY_PREFIX;
        this.rabbitTemplate = rabbitTemplate;
    }


    @Override
    public List<String> findConversationIds() {
        Set<String> keys = redisTemplate.keys(getPrefix() + "*");
        return keys.stream()
                .map(key -> key.substring(getKeyPrefix().length()))
                .collect(Collectors.toList());
    }

    private String getPrefix() {
        return keyPrefix;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        String key = getPrefix() + conversationId;
        List<String> messageStrings = redisTemplate.opsForList().range(key, 0, -1);
        if (CollectionUtils.isEmpty(messageStrings)) {
            return Collections.emptyList();
        }
        return messageStrings.stream().map(this::deserializeMessage).collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        String key = getPrefix() + conversationId;
        List<String> messageJsons = messages.stream().map(this::serializeMessage).toList();
        try (RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()) {
            connection.keyCommands().del(key.getBytes());
            if (!messageJsons.isEmpty()) {
                List<byte[]> values = new ArrayList<>();
                for (String messageJson : messageJsons) {
//                    if (isRouterMessage(messageJson)) continue;
                    values.add(messageJson.getBytes());
                }
                connection.listCommands().rPush(key.getBytes(),
                        values.toArray(new byte[values.size()][]));
            }
        }
        String value = messageJsons.get(messageJsons.size() - 1);
//        if (isRouterMessage(value)) return;
        if (key.contains("chat:router") || key.contains("chat:vector")) return;
        Map<String, Object> map = new HashMap<>(2);
        String[] split = key.split(":");
        if (split.length < 2) {
            logger.error("key format error: {}", key);
            return;
        }
        map.put("userId", split[2]);
        map.put("conversationId", split[3]);
        map.put("messageJson", value);
        //发送mq消息来存储信息
        rabbitTemplate.convertAndSend(
                AI_MESSAGE_DIRECT_EXCHANGE,
                AI_MESSAGE_ROUTING_KEY,
                map
        );
    }

    private boolean isRouterMessage(String s) {
        JSONObject entries = JSONUtil.parseObj(s);
        String textContent = entries.getStr("textContent");
        return ChatModelType.isCodeInChatModelType(textContent);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        redisTemplate.delete(getPrefix() + conversationId);
    }

    /**
     * Clear messages over the limit for a conversation
     *
     * @param conversationId the conversation ID
     * @param maxLimit maximum number of messages to keep
     * @param deleteSize number of messages to delete when over limit
     */
    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        String key = getPrefix() + conversationId;
        Long size = redisTemplate.opsForList().size(key);
        if (size < maxLimit) {
            return;
        }
        redisTemplate.opsForList().trim(key, deleteSize, -1);
    }


    @Override
    public void close() throws Exception {

    }
}
