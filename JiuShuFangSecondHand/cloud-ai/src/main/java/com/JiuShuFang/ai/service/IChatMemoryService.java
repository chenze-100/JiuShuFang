package com.JiuShuFang.ai.service;

import com.JiuShuFang.ai.domain.pojo.ChatMemory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @since 2026-04-21
 */
public interface IChatMemoryService extends IService<ChatMemory> {

    /**
     * 获取会话的所有消息（按时间排序）
     */
    List<ChatMemory> getConversationMessages(String conversationId);

    /**
     * 删除会话
     */
    void deleteConversation(String conversationId);

    /**
     * 保存AI回复
     */
    void saveAiMessage(String conversationId, Long userId, String content);

}
