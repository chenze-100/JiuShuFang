package com.JiuShuFangSecondHand.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户地址表
 * </p>
 *
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("address")
@Schema(description="用户地址表")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    @Schema(description = "收件人")
    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    private LocalDateTime createTime;

    private Integer isDeleted;


}
