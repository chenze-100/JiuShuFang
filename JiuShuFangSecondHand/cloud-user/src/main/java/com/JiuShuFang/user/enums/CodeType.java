package com.JiuShuFang.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeType {
    LOGIN_VERIFY(1, "登录验证"),
    FORGET_PASSWORD(2, "忘记密码"),
    CHANGE_PASSWORD(3, "修改密码");

    private final Integer code;
    private final String description;


    @JsonCreator
    public static CodeType getByCode(Object code) {
        if (code == null) {
            return null;
        }
        Integer codeInt = Integer.valueOf(code.toString());
        for (CodeType type : values()) {
            if (type.getCode().equals(codeInt)) {
                return type;
            }
        }
        return null;
    }
}
