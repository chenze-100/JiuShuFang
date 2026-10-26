package com.JiuShuFang.user.domain.pojo;

import lombok.Data;

@Data
public class WechatUser {
    private String openid;
    private String nickname;
    private Integer sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String unionid;
}
