package com.JiuShuFang.ai.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsToVectorVO {

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

    @Schema(description = "库存数量（二手书默认1）")
    private Integer stock;

    @Schema(description = "书籍封面图片")
    private String bookImg;

    @Schema(description = "书籍描述（新旧程度、笔记、破损等）")
    private String description;

    @Schema(description = "成色：全新 九成新 八成新 七成新及以下")
    private String degree;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "总数量")
    private Integer totalNumber;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
