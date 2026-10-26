package com.JiuShuFang.goods.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "上架书籍 DTO")
public class GoodsUploadDto {

    @Schema(description = "商品ID（修改时必填）")
    private Long id;

    @Schema(description = "书籍名称")
    @NotNull(message = "书籍名称不能为空")
    private String bookName;

    @Schema(description = "作者")
    @NotNull(message = "作者不能为空")
    private String author;

    @Schema(description = "出版社")
    @NotNull(message = "出版社不能为空")
    private String publisher;

    @Schema(description = "出售价格")
    @NotNull(message = "出售价格不能为空")
    @DecimalMin(value = "0.01", message = "出售价格不能小于0.01")
    private BigDecimal price;

    @Schema(description = "原价")
    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价不能小于0.01")
    private BigDecimal originalPrice;

    @Schema(description = "库存数量（二手书默认1）")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量不能小于1")
    private Integer stock;

    @Schema(description = "书籍封面图片")
    @NotNull(message = "书籍封面图片不能为空")
    private String bookImg;

    @Schema(description = "书籍描述（新旧程度、笔记、破损等）")
    @NotNull(message = "书籍描述不能为空")
    private String description;

    @Schema(description = "成色：1全新 2九成新 3八成新 4七成新及以下")
    @NotNull(message = "成色不能为空")
    @Min(value = 1, message = "成色不能小于1")
    @Max(value = 4, message = "成色不能大于4")
    private Integer degree;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
