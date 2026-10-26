package com.JiuShuFang.ai.service.impl;

import com.JiuShuFang.ai.domain.pojo.ChatMessage;
import com.JiuShuFang.ai.domain.pojo.ChatSession;
import com.JiuShuFang.ai.domain.query.ChatMessageQuery;
import com.JiuShuFang.ai.domain.vo.ChatMessageVO;
import com.JiuShuFang.ai.exception.UnauthorizedException;
import com.JiuShuFang.ai.mapper.ChatMessageMapper;
import com.JiuShuFang.ai.mapper.ChatSessionMapper;
import com.JiuShuFang.ai.service.IChatMessageService;
import com.JiuShuFang.common.util.BeanUtils;
import com.JiuShuFang.common.util.PageDTO;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @since 2026-04-18
 */
@RequiredArgsConstructor
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    private final ChatSessionMapper chatSessionMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void updateStatus(Long sessionId) {
        lambdaUpdate().in(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1)
                .update();
    }

    /**
     * 保存消息并设置最后一条消息
     * @param chatMessage
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAndSetLastMsg(ChatMessage chatMessage) {
        save(chatMessage);
        LambdaUpdateWrapper<ChatSession> wr = new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, chatMessage.getSessionId())
                .set(ChatSession::getLastMsg, chatMessage.getContent())
                .set(ChatSession::getLastTime, LocalDateTime.now());
        chatSessionMapper.update(wr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageDTO<ChatMessageVO> getChatMessages(ChatMessageQuery query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        
        Long sessionId = query.getSessionId();
        
        // 验证用户是否有权限查看该会话
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId))) {
            throw new UnauthorizedException("无权查看该会话");
        }
        
        // 创建分页对象，按创建时间倒序排序（最新的消息在前）
        Page<ChatMessage> page = new Page<>(query.getPageNo(), query.getPageSize());
        page.setOrders(java.util.Collections.singletonList(com.baomidou.mybatisplus.core.metadata.OrderItem.desc("create_time")));
        
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        
        Page<ChatMessage> resultPage = page(page, wrapper);
        List<ChatMessage> records = resultPage.getRecords();
        
        // 转换为 VO
        List<ChatMessageVO> voList = records.stream()
                .map(message -> BeanUtils.copyBean(message, ChatMessageVO.class))
                .collect(Collectors.toList());
        
        // 标记消息为已读（当前用户收到的消息）
        if (!voList.isEmpty()) {
            lambdaUpdate()
                    .eq(ChatMessage::getSessionId, sessionId)
                    .eq(ChatMessage::getIsRead, 0)
                    .set(ChatMessage::getIsRead, 1)
                    .update();
        }
        
        return PageDTO.of(resultPage, voList);
    }

}
