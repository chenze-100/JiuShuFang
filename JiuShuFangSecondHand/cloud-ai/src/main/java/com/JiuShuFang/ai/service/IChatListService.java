package com.JiuShuFang.ai.service;

import com.JiuShuFang.ai.domain.pojo.ChatList;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * ai聊天列表
 服务类
 * </p>
 *
 * @since 2026-05-06
 */
public interface IChatListService extends IService<ChatList> {

    List<ChatList> getUserConversationList();
}
