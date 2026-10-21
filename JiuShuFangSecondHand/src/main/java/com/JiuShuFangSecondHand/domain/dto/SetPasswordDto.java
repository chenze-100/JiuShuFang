package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@Schema(description = "忘记密码DTO")
public class SetPasswordDto {

    @Schema(description = "手机号")
    @NotNull(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "验证码")
    @NotNull(message = "验证码不能为空")
    private String code;

    @Schema(description = "新密码")
    @NotNull(message = "密码不能为空")
    private String password;
}
