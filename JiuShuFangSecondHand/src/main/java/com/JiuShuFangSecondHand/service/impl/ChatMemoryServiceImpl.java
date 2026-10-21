package com.JiuShuFangSecondHand.service.impl;

import cn.hutool.core.lang.Assert;
import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.domain.pojo.ChatList;
import com.JiuShuFangSecondHand.domain.pojo.ChatMemory;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.mapper.ChatMemoryMapper;
import com.JiuShuFangSecondHand.service.IChatListService;
import com.JiuShuFangSecondHand.service.IChatMemoryService;
import com.JiuShuFangSecondHand.service.IOrderService;
import com.JiuShuFangSecondHand.util.UserContext;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @since 2026-04-21
 */
@RequiredArgsConstructor
@Service
public class ChatMemoryServiceImpl extends ServiceImpl<ChatMemoryMapper, ChatMemory> implements IChatMemoryService {

    private final IOrderService orderService;
    private final IChatListService chatListService;
    private final StringRedisTemplate redisTemplate;



    @Override
    public List<ChatMemory> getConversationMessages(String conversationId) {
        //查询所有消息
        List<ChatMemory> list = lambdaQuery()
                .eq(ChatMemory::getConversationId, conversationId)
                .orderByAsc(ChatMemory::getTimestamp)
                .list();
        for (ChatMemory chatMemory : list) {
            //用户对话直接跳过
            if (chatMemory.getType().equals("USER")) continue;
            //查看是否有订单信息
            if (chatMemory.getOrderId() == null) continue;
            //如果有订单信息，则将订单信息转换为json字符串
            orderService.updateAIOrderInfo(chatMemory,
                    orderService.getById(chatMemory.getOrderId()));
        }
        return list;
    }


    @Transactional
    @Override
    public void deleteConversation(String conversationId) {
        Long userId = UserContext.getUserId();
        Assert.notNull(userId, () -> new UnauthorizedException("请先登录"));
        //删除会话和会话列表
        lambdaUpdate()
                .eq(ChatMemory::getConversationId, conversationId)
                .eq(ChatMemory::getUserId, userId)
                .remove();
        chatListService.lambdaUpdate()
                .eq(ChatList::getConversationId, conversationId)
                .eq(ChatList::getUserId, userId)
                .remove();
        //删除redis缓存
        String otherKey = RedisConstant.CHAT_MEMORY_LIST_KEY + conversationId + ":" + userId;
        String routerKey = RedisConstant.CHAT_SESSION_KEY + conversationId + ":" + userId;
        redisTemplate.unlink(List.of(otherKey, routerKey));
    }

    @Override
    public void saveAiMessage(String conversationId, Long userId, String content) {
        ChatMemory chatMemory = new ChatMemory()
                .setConversationId(conversationId)
                .setUserId(userId)
                .setContent(content)
                .setType("assistant")
                .setTimestamp(java.time.LocalDateTime.now());
        save(chatMemory);
    }

}
