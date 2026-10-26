package com.JiuShuFang.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BusinessErrorCode {
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PHONE_FORMAT_ERROR(1003, "手机号格式错误"),
    CODE_EXPIRED(1004, "验证码已过期"),
    CODE_ERROR(1005, "验证码错误"),
    LOGIN_FAILED(1006, "登录失败"),
    LOGIN_TIMEOUT(1007, "登录超时"),
    QR_CODE_EXPIRED(1008, "二维码已失效"),
    TOKEN_INVALID(1009, "Token无效"),
    TOKEN_EXPIRED(1010, "Token已过期"),
    PERMISSION_DENIED(1011, "权限不足"),
    PARAMETER_ERROR(1012, "参数错误"),
    DATABASE_ERROR(1013, "数据库操作失败"),
    REDIS_ERROR(1014, "Redis操作失败"),
    WECHAT_AUTH_FAILED(1015, "微信授权失败"),
    SYSTEM_ERROR(2001, "系统异常");

    private final int code;
    private final String message;
}
