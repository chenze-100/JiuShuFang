package com.JiuShuFangSecondHand.util;

public class UserContext {

    private static final ThreadLocal<Long> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setUser(Long userId) {
        CONTEXT_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
