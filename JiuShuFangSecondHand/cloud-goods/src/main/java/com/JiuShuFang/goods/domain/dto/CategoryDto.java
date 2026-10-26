package com.JiuShuFang.goods.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;


@Data
@Schema(description = "分类DTO")
public class CategoryDto {

    @Schema(description = "分类ID（修改时必填）")
    private Long id;

    @Schema(description = "分类名")
    @NotNull(message = "分类名不能为空")
    private String name;

    @Schema(description = "排序")
    private Integer sort;
}
