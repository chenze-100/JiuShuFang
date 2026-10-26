package com.JiuShuFang.goods.service;

import com.JiuShuFang.goods.domain.dto.CategoryDto;
import com.JiuShuFang.goods.domain.query.PageQuery;
import com.JiuShuFang.goods.domain.vo.CategoryVO;
import com.JiuShuFang.common.util.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.JiuShuFang.goods.domain.pojo.Category;

/**
 * <p>
 * 书籍分类 服务类
 * </p>
 *
 * @since 2026-04-09
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 新增分类
     *
     * @param dto 分类DTO
     */
    void addCategory(CategoryDto dto);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 修改分类
     *
     * @param dto 分类DTO
     */
    void updateCategory(CategoryDto dto);

    /**
     * 分页查询分类
     *
     * @param query 分页参数
     * @return 分页结果
     */
    PageDTO<CategoryVO> getCategoryList(PageQuery query);
}
