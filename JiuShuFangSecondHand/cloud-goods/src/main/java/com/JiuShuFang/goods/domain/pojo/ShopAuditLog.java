package com.JiuShuFang.goods.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商铺审核日志表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shop_audit_log")
@Schema(description = "商铺审核日志")
public class ShopAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "商铺ID")
    private Long shopId;

    @Schema(description = "操作人ID（管理员）")
    private Long operatorId;

    @Schema(description = "操作类型 1-创建 2-通过 3-拒绝 4-关闭")
    private Integer action;

    @Schema(description = "原因/备注")
    private String reason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @AllArgsConstructor
    @Getter
    public enum ActionEnum {
        CREATE(1, "创建"),
        APPROVE(2, "通过"),
        REJECT(3, "拒绝"),
        CLOSE(4, "关闭");

        private final int code;
        private final String desc;
    }
}
