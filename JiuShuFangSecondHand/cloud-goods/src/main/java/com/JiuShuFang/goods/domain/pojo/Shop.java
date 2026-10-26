package com.JiuShuFang.goods.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商铺表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("shop")
@Schema(description = "商铺表")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商铺ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
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

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除 0未删 1已删")
    private Integer isDeleted;

    /** 商铺状态枚举 */
    @AllArgsConstructor
    @Getter
    public enum StatusEnum {
        PENDING(0, "待审核"),
        APPROVED(1, "已通过"),
        REJECTED(2, "已拒绝"),
        CLOSED(3, "已关闭");

        private final int code;
        private final String desc;

        public static StatusEnum getByCode(int code) {
            for (StatusEnum status : StatusEnum.values()) {
                if (status.getCode() == code) return status;
            }
            return null;
        }
    }
}
