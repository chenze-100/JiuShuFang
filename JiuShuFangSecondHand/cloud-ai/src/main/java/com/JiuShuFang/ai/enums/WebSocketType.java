package com.JiuShuFang.ai.enums;

import cn.hutool.json.JSONUtil;
import com.JiuShuFang.ai.domain.pojo.ChatMessage;
import com.JiuShuFang.ai.domain.pojo.ChatSession;
// 微服务拆分：Goods/IGoodsService 属于 cloud-goods，IUserService 属于 cloud-user，第一阶段注释，后续改由 Feign 调用
// import com.JiuShuFang.ai.domain.pojo.Goods;
import com.JiuShuFang.ai.domain.vo.ChatMessageVO;
import com.JiuShuFang.ai.factory.impl.WebSocketMapSessionFactory;
import com.JiuShuFang.ai.service.IChatMessageService;
import com.JiuShuFang.ai.service.IChatSessionService;
// import com.JiuShuFang.ai.service.IGoodsService;
// import com.JiuShuFang.ai.service.IUserService;
import com.JiuShuFang.common.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Getter
@Slf4j
public enum WebSocketType {

    NOTIFICATION(0, "通知") {
        @Override
        public void close(Session session, Long userId) {
            var sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(NOTIFICATION);
            Session s = (Session) sessionMap
                    .get(userId);
            sessionMap.remove(userId); // 先移除
            // 自动关闭资源
            try (s) {
                log.info("移除通知连接: {}", userId);
            } catch (IOException e) {
                log.error("关闭连接失败", e);
            }
            log.info("移除用户{}的通知连接", userId);
        }

        @Override
        public void open(Session session, Long userId, IChatSessionService chatSessionService, IChatMessageService chatMessageService) {
            // 通知连接
            var sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(NOTIFICATION);
            sessionMap.put(userId, session);
            log.info("用户{}建立{}连接", userId, NOTIFICATION);
        }

        @Override
        public void message(Session session, ChatMessage chatMessage, Session chatSession) {
            // 发送普通通知
            if (chatSession != null && chatSession.isOpen()) {
                ChatMessageVO chatMessageVO = getChatMessageVONotInChat(session, chatMessage);

                try {
                    chatSession.getAsyncRemote().sendText(JSONUtil.toJsonStr(chatMessageVO));
                } catch (Exception e) {
                    log.error("通知发送失败", e);
                }
            }
        }
    },

    CHAT(1, "聊天") {
        @Override
        public void close(Session session, Long userId) {
            // 聊天连接从 CHAT_CONNECTIONS 移除
            String chatKey = (String) session.getUserProperties().get("chatKey");
            if (chatKey == null) {
                return;
            }
            Map sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(CHAT);
            Session s = (Session) sessionMap
                    .get(chatKey);
            sessionMap.remove(chatKey); // 先移除
            // 自动关闭资源
            try (s) {
                log.info("移除聊天连接: {}", chatKey);
            } catch (IOException e) {
                log.error("关闭连接失败", e);
            }
        }

        @Override
        public void open(Session session, Long userId, IChatSessionService chatSessionService, IChatMessageService chatMessageService) {
            // 聊天连接处理
            List<String> toUIdList = session.getRequestParameterMap().get("toUId");
            List<String> goodsIdList = session.getRequestParameterMap().get("goodsId");
            long toUId = Long.parseLong(toUIdList.get(0));
            long goodId = Long.parseLong(goodsIdList.get(0));
            // 微服务拆分：goodsService 属于 cloud-goods，第一阶段注释，后续改由 Feign 调用
            // Goods goods = goodsService.getById(goodId);
            // if (goods == null) {
            //     log.error("商品不存在，goodId={}", goodId);
            //     closeSession(session);
            //     return;
            // }
            // Long sellerId = goods.getUserId();
            Long sellerId = toUId; // TODO: 待接入 Feign 后改为 goods.getUserId()
            Long buyerId = userId;

            ChatSession chatSession = chatSessionService.lambdaQuery()
                    .eq(ChatSession::getBuyerId, buyerId)
                    .eq(ChatSession::getSellerId, sellerId)
                    .eq(ChatSession::getProductId, goodId)
                    .one();

            if (chatSession != null) {
                log.info("当前聊天已创建");
                chatMessageService.updateStatus(chatSession.getId());
            } else {
                chatSession = new ChatSession();
                chatSession.setBuyerId(buyerId);
                chatSession.setSellerId(sellerId);
                chatSession.setProductId(goodId);
                chatSessionService.saveAndUpdateChatMessageStatus(chatSession, toUId, userId);
            }

            // 生成聊天连接的唯一key
            String chatKey = WebSocketType.generateChatKey(userId, toUId, goodId);
            session.getUserProperties().put("chatKey", chatKey);
            session.getUserProperties().put("sessionId", chatSession.getId());
            session.getUserProperties().put("toUId", toUId);
            session.getUserProperties().put("productId", goodId);

            // 存储聊天连接
            Map<String, Session> sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(CHAT);
            sessionMap.put(chatKey, session);
            log.info("用户{}建立聊天连接，key: {}", userId, chatKey);

            // 给 toUser 发送已读通知 (sessionId)
            String targetChatKey = WebSocketType.generateChatKey(toUId, userId, goodId);
            Session targetSession = sessionMap.get(targetChatKey);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getAsyncRemote().sendText(chatSession.getId().toString());
                } catch (Exception e) {
                    log.error("向目标用户发送已读通知失败", e);
                }
            }

            // 给当前用户发送 sessionId，让前端可以立即加载历史消息
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(chatSession.getId().toString());
                } catch (Exception e) {
                    log.error("向当前用户发送 sessionId 失败", e);
                }
            }
            log.info("向用户{}发送 sessionId: {}", userId, chatSession.getId());

        }

        @Override
        public void message(Session session, ChatMessage chatMessage, Session chatSession) {
            // 用户正在对应的聊天页面，消息已读
            chatMessage.setIsRead(1);
            ChatMessageVO chatMessageVO = BeanUtils.copyBean(chatMessage, ChatMessageVO.class);
            // 微服务拆分：setUserInfoInSession 属于 cloud-user，第一阶段注释，后续改由 Feign 调用
            // userService.setUserInfoInSession(chatMessageVO);
            
            // 设置商品信息
            Object productIdObj = session.getUserProperties().get("productId");
            if (productIdObj != null) {
                chatMessageVO.setGoodsId(Long.parseLong(productIdObj.toString()));
            }

            try {
                // 发送已读状态给发送者
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(chatMessageVO.getSessionId().toString());
                }
                // 发送消息给接收者
                if (chatSession != null && chatSession.isOpen()) {
                    chatSession.getAsyncRemote().sendText(JSONUtil.toJsonStr(chatMessageVO));
                }
            } catch (Exception e) {
                log.error("消息发送失败", e);
            }
        }
    },

    CHAT_LIST(3, "聊天列表") {
        @Override
        public void close(Session session, Long userId) {
            // 聊天列表连接从 CHAT_LIST_CONNECTIONS 移除
            var sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(CHAT_LIST);
            Session s = (Session) sessionMap.get(userId);
            sessionMap.remove(userId); // 先移除
            // 自动关闭资源
            try (s) {
                log.info("移除聊天列表连接: {}", userId);
            } catch (IOException e) {
                log.error("关闭连接失败", e);
            }
            log.info("移除用户{}的聊天列表连接", userId);
        }

        @Override
        public void open(Session session, Long userId, IChatSessionService chatSessionService, IChatMessageService chatMessageService) {
            // 聊天列表连接
            Map sessionMap = WebSocketMapSessionFactory
                    .getSessionMap(CHAT_LIST);
            sessionMap.put(userId, session);
            log.info("用户{}建立{}连接", userId, CHAT_LIST);
        }

        @Override
        public void message(Session session, ChatMessage chatMessage, Session chatSession) {
            // 用户在聊天列表页面，发送新消息通知
            ChatMessageVO chatMessageVO = getChatMessageVONotInChat(session, chatMessage);

            try {
                if (chatSession != null && chatSession.isOpen()) {
                    chatSession.getAsyncRemote().sendText(JSONUtil.toJsonStr(chatMessageVO));
                    log.info("发送新消息通知到聊天列表页面");
                }
            } catch (Exception e) {
                log.error("聊天列表通知发送失败", e);
            }
        }
    };


    private static ChatMessageVO getChatMessageVONotInChat(Session session, ChatMessage chatMessage) {
        ChatMessageVO chatMessageVO = BeanUtils.copyBean(chatMessage, ChatMessageVO.class);
        chatMessageVO.setIsRead(0);
        // 微服务拆分：setUserInfoInSession 属于 cloud-user，第一阶段注释，后续改由 Feign 调用
        // userService.setUserInfoInSession(chatMessageVO);

        // 从发送者的 session 中获取商品信息
        if (session != null) {
            Object productIdObj = session.getUserProperties().get("productId");
            if (productIdObj != null) {
                chatMessageVO.setGoodsId(Long.parseLong(productIdObj.toString()));
            }
        }
        return chatMessageVO;
    }


    private final int type;
    private final String name;

    WebSocketType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    // 抽象方法(负责webSocket关闭连接)
    public abstract void close(Session session, Long userId);

    //抽象方法(负责webSocket开启连接)
    public abstract void open(Session session, Long userId, IChatSessionService chatSessionService, IChatMessageService chatMessageService);

    //抽象方法(负责webSocket发送连接的key)
    public abstract void message(Session session, ChatMessage chatMessage, Session chatSession);


    /**
     * 生成聊天连接的唯一key
     * @param userId 当前用户ID
     * @param toUId 对方用户ID
     * @param productId 商品ID
     * @return 唯一key
     */
    public static String generateChatKey(long userId, long toUId, long productId) {
        // 不使用min/max，保持方向性，确保A->B和B->A是不同的连接
        return userId + "_" + toUId + "_" + productId;
    }

    public static void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("关闭会话失败", e);
            }
        }
    }

    @JsonCreator
    public static WebSocketType getByCode(Object code) {
        if (code == null) {
            return null;
        }
        int codeInt = Integer.parseInt(code.toString());
        return Arrays.stream(values()).
                filter(type -> type.getType() == codeInt).
                findFirst()
                .orElse(null);
    }
}