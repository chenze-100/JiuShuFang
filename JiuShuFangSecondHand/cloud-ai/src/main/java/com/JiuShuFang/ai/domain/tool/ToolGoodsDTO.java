package com.JiuShuFang.ai.domain.tool;

import java.math.BigDecimal;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.ai.tool.annotation.ToolParam;

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
public class ToolGoodsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ToolParam(description = "书籍名称")
    private String bookName;

    @ToolParam(description = "作者")
    private String author;

    @ToolParam(description = "出版社")
    private String publisher;

    @ToolParam(description = "出售价格")
    private BigDecimal price;

    @ToolParam(description = "原价")
    private BigDecimal originalPrice;

    @ToolParam(description = "书籍封面图片")
    private String bookImg;

    @ToolParam(description = "书籍描述（新旧程度、笔记、破损等）")
    private String description;

    @ToolParam(description = "成色：1全新 2九成新 3八成新 4七成新及以下")
    private Integer degree;

    @ToolParam(description = "分类ID")
    private Long categoryId;

    @ToolParam(description = "总数量")
    private Integer totalNumber;


}
