package com.JiuShuFang.user.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(description = "User对象")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户名")
    private String nickName;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "登录类型 1-手机号 2-微信扫码")
    private Integer loginType;

    @Schema
    private BigDecimal balance;

    // ====================== 微信登录专用字段 ======================
    @Schema(description = "微信openid（唯一标识）")
    private String openid;

    @Schema(description = "微信unionid（跨平台唯一）")
    private String unionid;

    @Schema(description = "性别 0-未知 1-男 2-女")
    private Integer sex;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "真实姓名（实名认证）")
    private String realName;

    @Schema(description = "身份证号（实名认证）")
    private String idCard;

    @Schema(description = "实名认证状态 0-未认证 1-已认证")
    private Integer realNameStatus;
}
