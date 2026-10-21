package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.BookHistoryDto;
import com.JiuShuFangSecondHand.domain.query.PageQuery;
import com.JiuShuFangSecondHand.domain.vo.HistoryGoodsVO;
import com.JiuShuFangSecondHand.util.PageDTO;
import com.JiuShuFangSecondHand.util.Result;

/**
 * <p>
 * 浏览历史服务类
 * </p>
 *
 * @since 2026-04-11
 */
public interface IHistoryService {

    /**
     * 添加浏览历史
     *
     * @param dto 浏览历史 DTO
     * @return 结果
     */
    Result addHistory(BookHistoryDto dto);


    /**
     * 分页查询浏览历史
     *
     * @return 分页结果
     */
    Result<PageDTO<HistoryGoodsVO>> getHistoryPage(PageQuery pageQuery);
}
