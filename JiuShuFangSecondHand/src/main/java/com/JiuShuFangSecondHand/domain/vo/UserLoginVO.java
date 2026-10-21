package com.JiuShuFangSecondHand.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserLoginVO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名/微信昵称")
    private String nickName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "登录令牌 JWT")
    private String token;

    // ===================== 微信登录扩展字段 =====================
    @Schema(description = "微信openid")
    private String openid;

    @Schema(description = "微信头像")
    private String avatar;

    @Schema(description = "性别 0-未知 1-男 2-女")
    private Integer sex;

    @Schema(description = "登录类型 1-账号密码 2-微信扫码")
    private Integer loginType;
}