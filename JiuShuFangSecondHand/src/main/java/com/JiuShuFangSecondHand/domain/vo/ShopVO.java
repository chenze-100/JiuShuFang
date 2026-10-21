package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商铺信息 VO
 */
@Data
@Schema(description = "商铺信息")
public class ShopVO {

    @Schema(description = "商铺ID")
    private Long id;

    @Schema(description = "店主用户ID")
    private Long userId;

    @Schema(description = "商铺名称")
    private String name;

    @Schema(description = "商铺分类ID")
    private Long categoryId;

    @Schema(description = "商铺Logo")
    private String logoUrl;

    @Schema(description = "商铺简介")
    private String intro;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态：0-待审核 1-已通过 2-已拒绝 3-已关闭")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
