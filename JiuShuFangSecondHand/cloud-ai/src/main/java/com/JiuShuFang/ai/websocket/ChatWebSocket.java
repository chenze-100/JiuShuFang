package com.JiuShuFang.ai.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFang.ai.domain.pojo.ChatMessage;
import com.JiuShuFang.ai.enums.WebSocketType;
import com.JiuShuFang.ai.factory.impl.WebSocketMapSessionFactory;
import com.JiuShuFang.ai.service.IChatMessageService;
import com.JiuShuFang.ai.service.IChatSessionService;
// 微服务拆分：IGoodsService 属于 cloud-goods、IUserService 属于 cloud-user，第一阶段注释，后续改由 Feign 调用
// import com.JiuShuFang.ai.service.IGoodsService;
// import com.JiuShuFang.ai.service.IUserService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@ServerEndpoint("/chat/{sessionUid}")
@Component
public class ChatWebSocket {

    // 通知连接Map：key是userId，value是Session
    private static final Map<Long, Session> NOTIFICATION_CONNECTIONS =
            WebSocketMapSessionFactory.getSessionMap(WebSocketType.NOTIFICATION);

    // 聊天连接Map：key是 "userId_toUId_productId"，value是Session
    private static final Map<String, Session> CHAT_CONNECTIONS =
            WebSocketMapSessionFactory.getSessionMap(WebSocketType.CHAT);

    // 聊天列表连接Map：key是userId，value是Session
    private static final Map<Long, Session> CHAT_LIST_CONNECTIONS =
            WebSocketMapSessionFactory.getSessionMap(WebSocketType.CHAT_LIST);

    private static IChatSessionService chatSessionService;
    private static IChatMessageService chatMessageService;
    // 微服务拆分：跨服务依赖第一阶段注释
    // private static IGoodsService goodsService;
    // private static IUserService userService;

    @Autowired
    public void setChatSessionService(IChatSessionService chatSessionService) {
        ChatWebSocket.chatSessionService = chatSessionService;
    }

    @Autowired
    public void setChatMessageService(IChatMessageService chatMessageService) {
        ChatWebSocket.chatMessageService = chatMessageService;
    }

    // @Autowired
    // public void setGoodsService(IGoodsService goodsService) {
    //     ChatWebSocket.goodsService = goodsService;
    // }

    // @Autowired
    // public void setUserService(IUserService userService) {
    //     ChatWebSocket.userService = userService;
    // }

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionUid") Long userId) {
        log.info("用户{}上线", userId);
        if (userId == null) {
            log.error("userId is null");
            WebSocketType.closeSession(session);
            return;
        }

        List<String> toUIdList = session.getRequestParameterMap().get("toUId");
        List<String> goodsIdList = session.getRequestParameterMap().get("goodsId");

        if (CollUtil.isEmpty(toUIdList)) {
            log.error("toUId or goodsId is null");
            WebSocketType.closeSession(session);
            return;
        }

        // 判断连接类型
        int connectionType = WebSocketType.NOTIFICATION.getType(); // 默认通知连接
        if (CollUtil.isNotEmpty(goodsIdList)) {
            String goodsId = goodsIdList.get(0);
            if ("0".equals(goodsId)) {
                // 检查是否是聊天列表连接（通过特殊参数标识）
                List<String> typeList = session.getRequestParameterMap().get("type");
                if (CollUtil.isNotEmpty(typeList) && "3".equals(typeList.get(0))) {
                    connectionType = WebSocketType.CHAT_LIST.getType(); // 聊天列表连接
                }
            } else {
                connectionType = WebSocketType.CHAT.getType(); // 聊天连接
            }
        }

        // 存储对应类型的连接
        session.getUserProperties().put("connectionType", connectionType);
        // 存储对应类型的连接
        WebSocketType.getByCode(connectionType)
                .open(session, userId, chatSessionService, chatMessageService);
    }


    @OnMessage
    public void onMessage(Session session, String msgJson, @PathParam("sessionUid") Long fromId) {
        ChatMessage chatMessage = JSONUtil.toBean(msgJson, ChatMessage.class);
        Object sessionIdObj = session.getUserProperties().get("sessionId");
        Object toUIdObj = session.getUserProperties().get("toUId");
        Object productIdObj = session.getUserProperties().get("productId");

        if (sessionIdObj == null || toUIdObj == null || productIdObj == null) {
            log.error("sessionId or toUId or productId is null");
            return;
        }
        chatMessage.setSessionId(Long.parseLong(sessionIdObj.toString()));
        chatMessage.setIsRead(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        // 先保存消息到数据库
        chatMessageService.saveAndSetLastMsg(chatMessage);

        // 再发送消息通知
        long productId = Long.parseLong(productIdObj.toString());
        String targetChatKey = WebSocketType.generateChatKey(chatMessage.getToId(), fromId, productId);
        Session chatSession = CHAT_CONNECTIONS.get(targetChatKey);
        sendMessage(session, chatSession, chatMessage);
    }

    private static void sendMessage(Session session, Session chatSession, ChatMessage chatMessage) {
        if (chatSession != null && chatSession.isOpen()) {
            // 用户正在对应的聊天页面
            WebSocketType.CHAT.message(session, chatMessage, chatSession);
        } else {
            // 用户不在聊天页面，检查是否有聊天列表连接
            Session chatListSession = CHAT_LIST_CONNECTIONS.get(chatMessage.getToId());
            if (chatListSession != null && chatListSession.isOpen()) {
                // 用户在聊天列表页面，发送新消息通知
                WebSocketType.CHAT_LIST.message(session, chatMessage, chatListSession);
            } else {
                // 发送普通通知
                WebSocketType.NOTIFICATION.message(session, chatMessage,
                        NOTIFICATION_CONNECTIONS.get(chatMessage.getToId()));
            }
        }
    }


    @OnClose
    public void onClose(Session session, @PathParam("sessionUid") Long userId) {
        log.info("用户{}下线", userId);
        Integer connectionType = (Integer) session.getUserProperties().get("connectionType");
        if (connectionType == null) {
            log.debug("connectionType is null");
            return;
        }
        //策略模式直接调用
        WebSocketType.getByCode(connectionType)
                .close(session, userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
    }


}
