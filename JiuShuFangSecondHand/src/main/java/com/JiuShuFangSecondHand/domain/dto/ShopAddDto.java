package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 添加商铺 DTO
 */
@Data
@Schema(description = "添加商铺请求")
public class ShopAddDto {

    @NotBlank(message = "商铺名称不能为空")
    @Schema(description = "商铺名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "商铺分类ID")
    private Long categoryId;

    @Schema(description = "商铺Logo URL")
    private String logoUrl;

    @Schema(description = "商铺简介")
    private String intro;

    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPhone;
}
