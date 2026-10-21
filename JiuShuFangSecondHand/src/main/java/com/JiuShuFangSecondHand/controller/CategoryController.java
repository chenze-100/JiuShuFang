package com.JiuShuFangSecondHand.controller;

import com.JiuShuFangSecondHand.domain.dto.CategoryDto;
import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.CategoryVO;
import com.JiuShuFangSecondHand.service.ICategoryService;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 * 书籍分类 前端控制器
 * </p>
 *
 * @since 2026-04-09
 */
@Tag(name = "分类", description = "分类相关接口")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public Result<String> addCategory(@Valid @RequestBody CategoryDto dto) {
        categoryService.addCategory(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @PutMapping
    public Result<String> updateCategory(@Valid @RequestBody CategoryDto dto) {
        categoryService.updateCategory(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageDTO<CategoryVO>> getCategoryList(PageQuery query) {
        PageDTO<CategoryVO> result = categoryService.getCategoryList(query);
        return Result.success(result);
    }
}
