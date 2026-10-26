package com.JiuShuFang.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN(1),
    USER(2);
    private final Integer code;
}
