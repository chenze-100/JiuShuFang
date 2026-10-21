package com.JiuShuFangSecondHand.service.impl;

import com.JiuShuFangSecondHand.domain.pojo.ChatList;
import com.JiuShuFangSecondHand.mapper.ChatListMapper;
import com.JiuShuFangSecondHand.service.IChatListService;
import com.JiuShuFangSecondHand.util.UserContext;
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
