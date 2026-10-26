package com.JiuShuFang.trade.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "新增地址DTO")
public class AddressDto {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收件人")
    @NotNull(message = "收件人不能为空")
    private String receiver;

    @Schema(description = "手机号")
    @NotNull(message = "手机号不能为空")
    private String phone;

    @Schema(description = "省份")
    @NotNull(message = "省份不能为空")
    private String province;

    @Schema(description = "城市")
    @NotNull(message = "城市不能为空")
    private String city;

    @Schema(description = "区县")
    @NotNull(message = "区县不能为空")
    private String district;

    @Schema(description = "详细地址")
    @NotNull(message = "详细地址不能为空")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault = false;
}
