package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.query.ChatMessageQuery;
import com.JiuShuFangSecondHand.domain.vo.ChatMessageVO;
import com.JiuShuFangSecondHand.domain.pojo.ChatMessage;
import com.JiuShuFangSecondHand.util.PageDTO;
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
