package com.JiuShuFangSecondHand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "添加浏览历史 DTO")
public class BookHistoryDto {

    @NotNull(message = "书籍 ID 不能为空")
    @Schema(description = "书籍 ID")
    private Long bookId;
}
