package com.JiuShuFangSecondHand.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "浏览历史商品 VO")
public class HistoryGoodsVO{


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

    @Schema(description = "商品状态：1上架 2下架 3已售")
    private Integer status;


    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否收藏")
    private Boolean isLike = false;

}
