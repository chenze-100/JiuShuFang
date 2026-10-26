package com.JiuShuFang.ai.service.impl;

import com.JiuShuFang.ai.domain.pojo.ChatList;
import com.JiuShuFang.ai.mapper.ChatListMapper;
import com.JiuShuFang.ai.service.IChatListService;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * ai聊天列表
 服务实现类
 * </p>
 *
 * @since 2026-05-06
 */
@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements IChatListService {

    @Override
    public List<ChatList> getUserConversationList() {
        Long userId = UserContext.getUserId();
        if(userId == null){
            return Collections.emptyList();
        }
        List<ChatList> list = lambdaQuery()
                .eq(ChatList::getUserId, userId)
                .orderByDesc(ChatList::getCreateTime)
                .list();
        if(list.isEmpty()){
            return Collections.emptyList();
        }
        return list;
    }
}
