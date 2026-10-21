package com.JiuShuFangSecondHand.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatModelType {

    ROUTER("router",
            "qwen3.5-plus",
            "路由模型：判断用户意图（购买/上架/普通）"),
    BUY("buy",
            "qwen3.5-plus",
            "购买模型：处理找书、买书、订单相关"),
    SELL("sell",
            "qwen3.5-plus",
            "上架模型：处理上架商品流程"),
    NORMAL("normal",
            "qwen3.5-plus",
            "普通模型：处理打招呼、寒暄、违规内容等非交易请求");

    private final String code;
    private final String modelName;
    private final String description;

    /**
     * 根据 code 查找枚举，未找到返回 null
     */
    public static ChatModelType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChatModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }


    /**
     * 根据 code 查找枚举，未找到返回 null
     */
    public static boolean isCodeInChatModelType(String code) {
        if (code == null) {
            return false;
        }
        for (ChatModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据路由模型返回内容解析出对应枚举
     */
    public static ChatModelType fromContent(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("buy") || lower.contains("购买") || lower.contains("买书") || lower.contains("找书")) {
            return BUY;
        }
        if (lower.contains("sell") || lower.contains("上架") || lower.contains("卖书") || lower.contains("listing")) {
            return SELL;
        }
        if (lower.contains("normal") || lower.contains("普通")) {
            return NORMAL;
        }
        return null;
    }

}
