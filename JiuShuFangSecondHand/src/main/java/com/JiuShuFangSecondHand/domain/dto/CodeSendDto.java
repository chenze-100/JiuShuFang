package com.JiuShuFangSecondHand.domain.dto;

import com.JiuShuFangSecondHand.enums.CodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

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
