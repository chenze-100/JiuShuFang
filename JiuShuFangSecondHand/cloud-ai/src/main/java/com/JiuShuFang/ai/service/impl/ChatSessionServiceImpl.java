package com.JiuShuFang.ai.service.impl;

import com.JiuShuFang.ai.domain.pojo.ChatMessage;
import com.JiuShuFang.ai.domain.pojo.ChatSession;
// 微服务拆分：User/Goods/UserMapper/GoodsMapper 属于 cloud-user / cloud-goods，第一阶段注释，后续改由 Feign 调用
// import com.JiuShuFang.ai.domain.pojo.Goods;
// import com.JiuShuFang.ai.domain.pojo.User;
import com.JiuShuFang.ai.domain.query.PageQuery;
import com.JiuShuFang.ai.domain.vo.ChatSessionVO;
import com.JiuShuFang.ai.exception.UnauthorizedException;
import com.JiuShuFang.ai.mapper.ChatSessionMapper;
// import com.JiuShuFang.ai.mapper.GoodsMapper;
// import com.JiuShuFang.ai.mapper.UserMapper;
import com.JiuShuFang.ai.service.IChatMessageService;
import com.JiuShuFang.ai.service.IChatSessionService;
import com.JiuShuFang.common.util.BeanUtils;
import com.JiuShuFang.common.util.CollUtils;
import com.JiuShuFang.common.util.PageDTO;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @since 2026-04-18
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

    private final IChatMessageService chatMessageService;
    // 微服务拆分：跨服务 Mapper 第一阶段注释，后续改由 Feign 调用
    // private final UserMapper userMapper;
    // private final GoodsMapper goodsMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void saveAndUpdateChatMessageStatus(ChatSession chatSession, Long fromId, Long toId) {
        save(chatSession);
        chatMessageService.updateStatus(chatSession.getId());
    }

    @Override
    public PageDTO<ChatSessionVO> getChatSessions(PageQuery query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        Page<ChatSession> page = query.toMpPageDefaultSortByCreatedTimeDesc();
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getBuyerId, userId)
                .or()
                .eq(ChatSession::getSellerId, userId);

        Page<ChatSession> resultPage = page(page, wrapper);
        List<ChatSession> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageDTO.empty(resultPage);
        }

        // 收集所有用户ID
        Set<Long> userIds = new HashSet<>();
        // 收集所有商品ID
        Set<Long> productIds = new HashSet<>();
        for (ChatSession session : records) {
            if (!session.getBuyerId().equals(userId)) {
                userIds.add(session.getBuyerId());
            }
            if (!session.getSellerId().equals(userId)) {
                userIds.add(session.getSellerId());
            }
            if (session.getProductId() != null) {
                productIds.add(session.getProductId());
            }
        }

        // 微服务拆分：批量查询用户/商品信息依赖 cloud-user / cloud-goods，第一阶段注释，后续改由 Feign 调用
        // // 批量查询用户信息
        // List<User> users = userMapper.selectBatchIds(userIds);
        // Map<Long, User> userMap = users.stream()
        //         .collect(Collectors.toMap(User::getId, u -> u));

        // // 批量查询商品信息
        // List<Goods> goodsList = new ArrayList<>();
        // if (!productIds.isEmpty()) {
        //     goodsList = goodsMapper.selectBatchIds(productIds);
        // }
        // Map<Long, Goods> goodsMap = goodsList.stream()
        //         .collect(Collectors.toMap(Goods::getId, g -> g));
        Map<Long, Object> userMap = new HashMap<>();
        Map<Long, Object> goodsMap = new HashMap<>();

        Map<Long, Long> sessionIdToNotReadCountMap = getMyNotReadSession(userId);


        // 组装VO
        List<ChatSessionVO> voList = records.stream().map(session -> {
            ChatSessionVO vo = BeanUtils.copyBean(session, ChatSessionVO.class);

            // 确定对方用户ID
            Long otherUserId = session.getBuyerId().equals(userId)
                    ? session.getSellerId()
                    : session.getBuyerId();
            vo.setUserId(otherUserId);

            // 获取对方用户信息（跨服务查询已注释，暂显示"未知用户"）
            // User otherUser = (User) userMap.get(otherUserId);
            // if (otherUser != null) {
            //     vo.setName(otherUser.getNickName());
            //     vo.setAvator(otherUser.getAvatar());
            // } else {
            //     vo.setName("未知用户");
            //     vo.setAvator("");
            // }
            vo.setName("未知用户");
            vo.setAvator("");

            // 计算未读消息数量
            Long notReadCount = sessionIdToNotReadCountMap
                    .getOrDefault(session.getId(), 0L);

            vo.setNotReadNum(notReadCount.intValue());

            // 设置商品信息（跨服务查询已注释，暂显示空）
            // Goods goods = (Goods) goodsMap.get(session.getProductId());
            // if (goods != null) {
            //     vo.setProductName(goods.getBookName());
            //     vo.setProductImage(goods.getBookImg());
            //     vo.setPrice(goods.getPrice() != null ? goods.getPrice().toString() : "0");
            // } else {
            //     vo.setProductName("");
            //     vo.setProductImage("");
            //     vo.setPrice("0");
            // }
            vo.setProductName("");
            vo.setProductImage("");
            vo.setPrice("0");

            return vo;
        }).collect(Collectors.toList());

        return PageDTO.of(resultPage, voList);
    }

    private Map<Long, Long> getMyNotReadSession(Long userId) {
        //获取所有发给我的未读消息
        List<ChatMessage> chatMessages = chatMessageService.lambdaQuery()
                .eq(ChatMessage::getToId, userId)
                .eq(ChatMessage::getIsRead, 0)
                .select(ChatMessage::getSessionId)
                .list();

        Map<Long, Long> sessionIdToNotReadCountMap = new HashMap<>();
        //获取每个sessionId的未读消息数量
        if (CollUtils.isNotEmpty(chatMessages)) {
            sessionIdToNotReadCountMap = chatMessages.stream()
                    .collect(Collectors.groupingBy(
                            ChatMessage::getSessionId,
                            Collectors.counting()));
        }
        return sessionIdToNotReadCountMap;
    }
}
