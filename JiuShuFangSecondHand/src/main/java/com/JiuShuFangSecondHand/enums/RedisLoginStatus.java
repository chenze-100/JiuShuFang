package com.JiuShuFangSecondHand.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
