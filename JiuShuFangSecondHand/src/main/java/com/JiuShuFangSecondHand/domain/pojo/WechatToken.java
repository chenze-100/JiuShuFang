package com.JiuShuFangSecondHand.domain.pojo;

import lombok.Data;

@Data
public class WechatToken {
    private String access_token;
    private String openid;
    private String refresh_token;
    private Integer expires_in;
    private String scope;
}