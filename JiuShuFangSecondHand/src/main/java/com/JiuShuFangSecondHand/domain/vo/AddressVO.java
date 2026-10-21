package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "地址VO")
public class AddressVO {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收件人")
    private String receiver;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;
}
