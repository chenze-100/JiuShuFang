package com.JiuShuFang.ai.domain.tool;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;

@Data
public class ToolCategoryVO {

    @ToolParam(description = "分类ID")
    private Long id;

    @ToolParam(description = "分类名")
    private String name;

    @ToolParam(description = "排序")
    private Integer sort;

    @ToolParam(description = "创建时间")
    private LocalDateTime createTime;
}


