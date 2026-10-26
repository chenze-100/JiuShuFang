package com.JiuShuFang.ai.service;

import com.JiuShuFang.ai.domain.query.PageQuery;
import com.JiuShuFang.ai.domain.vo.ChatSessionVO;
import com.JiuShuFang.ai.domain.pojo.ChatSession;
import com.JiuShuFang.common.util.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @since 2026-04-18
 */
public interface IChatSessionService extends IService<ChatSession> {

    /**
     * 保存并更新消息状态
     * @param chatSession 会话
     * @param toUId 接收者ID
     * @param userId 发送者ID
     */
    void saveAndUpdateChatMessageStatus(ChatSession chatSession, Long toUId, Long userId);

    /**
     * 分页查询聊天会话
     * @param query 分页查询参数
     * @return 分页结果
     */
    PageDTO<ChatSessionVO> getChatSessions(PageQuery query);
}
