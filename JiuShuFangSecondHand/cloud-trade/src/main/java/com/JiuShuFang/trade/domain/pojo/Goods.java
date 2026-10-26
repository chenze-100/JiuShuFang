package com.JiuShuFang.trade.domain.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商品表（二手书）
 * </p>
 *
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("goods")
@Schema(description="商品表（二手书）")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "书籍名称")
    private String bookName;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "出版社")
    private String publisher;

    @Schema(description = "出售价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "库存数量（二手书默认1）")
    private Integer stock;

    @Schema(description = "书籍封面图片")
    private String bookImg;

    @Schema(description = "书籍描述（新旧程度、笔记、破损等）")
    private String description;

    @Schema(description = "成色：1全新 2九成新 3八成新 4七成新及以下")
    private Integer degree;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "总数量")
    private Integer totalNumber;

    @Schema(description = "商品状态：1上架 2下架 3已售")
    private Integer status;

    @Schema(description = "逻辑删除 0未删 1已删")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


    @AllArgsConstructor
    @Getter
    public enum StatusEnum {
        ON_SALE(1, "上架"),
        OFF_SALE(2, "下架"),
        SOLD(3, "已售");

        private final int code;
        private final String desc;

        public static StatusEnum getByCode(int code) {
            for (StatusEnum status : StatusEnum.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null;
        }
    }


    @AllArgsConstructor
    @Getter
    public enum DegreeEnum {
        NEW(1, "全新"),
        NINE_TENTH(2, "九成新"),
        EIGHT_TENTH(3, "八成新"),
        SEVEN_TENTH_AND_BELOW(4, "七成新及以下");

        private final int code;
        private final String desc;


        public static DegreeEnum getByCode(int code) {
            for (DegreeEnum degree : DegreeEnum.values()) {
                if (degree.getCode() == code) {
                    return degree;
                }
            }
            return null;
        }
    }
}
