package com.JiuShuFang.user.constants;

public interface RedisConstant {
    //登录注册相关
    String LOGIN_CODE_KEY = "login:code:";
    String LOGIN_ID_KEY = "login:id:";
    String LOGIN_USERINFO_KEY = "login:userinfo:";
    String LOGIN_TOKEN_KEY = "login:token:";
    
    //验证码相关
    String CODE_SEND_KEY = "code:send:";

    //聊天Session相关
    String CHAT_SESSION_KEY = "chat:session:";
}
