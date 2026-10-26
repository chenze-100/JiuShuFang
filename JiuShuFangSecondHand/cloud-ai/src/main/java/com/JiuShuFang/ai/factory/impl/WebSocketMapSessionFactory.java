package com.JiuShuFang.ai.factory.impl;

import com.JiuShuFang.ai.enums.WebSocketType;
import jakarta.websocket.Session;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketMapSessionFactory {


    // 2. 全局唯一的Session仓库
    private static final Map<WebSocketType, Map> SESSION_MAP = new EnumMap<>(WebSocketType.class);

    static {
        SESSION_MAP.put(WebSocketType.NOTIFICATION, new ConcurrentHashMap<Long, Session>());
        SESSION_MAP.put(WebSocketType.CHAT, new ConcurrentHashMap<String, Session>());
        SESSION_MAP.put(WebSocketType.CHAT_LIST, new ConcurrentHashMap<Long, Session>());
    }

    private WebSocketMapSessionFactory() {
    }

    // 根据类型获取对应的Map
    public static Map getSessionMap(WebSocketType type) {
        return SESSION_MAP.get(type);
    }
}