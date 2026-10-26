package com.JiuShuFang.user.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFang.user.domain.pojo.WechatToken;
import com.JiuShuFang.user.domain.pojo.WechatUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WxCodeUtil {

    public static String appid;
    public static String secret;

    @Value("${wx.appid}")
    public static void setAppid(String appid) {
        WxCodeUtil.appid = appid;
    }
    @Value("${wx.secret}")
    public static void setSecret(String secret) {
        WxCodeUtil.secret = secret;
    }




    public static WechatUser getWxCode(String code) {

        // 1. 获取微信 token
        String tokenUrl = StrUtil.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code",
                appid, secret, code
        );
        String tokenJson = HttpUtil.get(tokenUrl);
        WechatToken token = JSONUtil.toBean(tokenJson, WechatToken.class);

        // 2. 获取微信用户信息
        String userUrl = StrUtil.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token={}&openid={}&lang=zh_CN",
                token.getAccess_token(), token.getOpenid()
        );
        return JSONUtil.toBean(HttpUtil.get(userUrl), WechatUser.class);
    }
}
