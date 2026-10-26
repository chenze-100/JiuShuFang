package com.JiuShuFang.ai.service;

import com.JiuShuFang.ai.domain.query.ChatMessageQuery;
import com.JiuShuFang.ai.domain.vo.ChatMessageVO;
import com.JiuShuFang.ai.domain.pojo.ChatMessage;
import com.JiuShuFang.common.util.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @since 2026-04-18
 */
public interface IChatMessageService extends IService<ChatMessage> {


    void updateStatus(Long sessionId);


    void saveAndSetLastMsg(ChatMessage chatMessage);

    /**
     * 分页查询聊天消息
     * @param query 查询参数
     * @return 分页结果
     */
    PageDTO<ChatMessageVO> getChatMessages(ChatMessageQuery query);
}
