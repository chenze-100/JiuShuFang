package com.JiuShuFangSecondHand.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.domain.pojo.WechatToken;
import com.JiuShuFangSecondHand.domain.pojo.WechatUser;

public class WxCodeUtil {

    public static WechatUser getWxCode(String code) {
        String APPID = "wxaca57064625d7156";
        String SECRET = "40d4fe84a3c299c9a21f5db4d0f5f454";
        // 1. 获取微信 token
        String tokenUrl = StrUtil.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code",
                APPID, SECRET, code
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
