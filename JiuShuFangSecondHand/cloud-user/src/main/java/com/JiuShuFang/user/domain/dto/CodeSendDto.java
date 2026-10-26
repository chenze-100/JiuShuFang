package com.JiuShuFang.user.domain.dto;

import com.JiuShuFang.user.enums.CodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送验证码 DTO")
public class CodeSendDto {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @NotNull(message = "验证码类型不能为空")
    @Schema(description = "验证码类型")
    private CodeType type;
}
