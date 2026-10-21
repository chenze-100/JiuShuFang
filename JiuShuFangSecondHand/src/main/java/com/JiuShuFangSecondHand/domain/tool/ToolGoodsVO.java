package com.JiuShuFangSecondHand.domain.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ToolGoodsVO {

    @Schema(description = "商品ID")
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

    @Schema(description = "书籍封面图片")
    private String bookImg;

    @Schema(description = "书籍描述（新旧程度、笔记、破损等）")
    private String description;

    @Schema(description = "库存数量（二手书默认1）")
    private Integer stock;

    @Schema(description = "总库存数量")
    public Integer totalNumber;

    @Schema(description = "成色：1全新 2九成新 3八成新 4七成新及以下")
    private Integer degree;


    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "商品状态：1上架 2下架 3已售")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否收藏")
    private Boolean isLike = false;
}
