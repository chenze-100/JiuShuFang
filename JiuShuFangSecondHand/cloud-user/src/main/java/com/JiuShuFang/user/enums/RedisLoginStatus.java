package com.JiuShuFang.user.enums;

import lombok.Getter;

@Getter
public enum RedisLoginStatus {
    WAIT(0),
    LOGINING(1),
    LOGIN_SUCCESS(2);

    private final Integer code;

    RedisLoginStatus(Integer code) {
        this.code = code;
    }
}
