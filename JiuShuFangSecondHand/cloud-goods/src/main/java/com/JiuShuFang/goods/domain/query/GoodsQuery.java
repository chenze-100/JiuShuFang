package com.JiuShuFang.goods.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品分页查询参数")
public class GoodsQuery extends PageQuery {

    @Schema(description = "书籍名称（模糊查询）")
    private String bookName;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "商品状态：1上架 2下架 3已售")
    private Integer status;

    @Schema(description = "是否查看卖家店铺")
    private Boolean isViewSellerStore = false;

    @Schema(description = "卖家id")
    private String sellerId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类ID集合")
    private List<Long> categoryIds;

    @Schema(description = "成色：1全新 2九成新 3八成新 4七成新及以下")
    private Integer degree;
}
