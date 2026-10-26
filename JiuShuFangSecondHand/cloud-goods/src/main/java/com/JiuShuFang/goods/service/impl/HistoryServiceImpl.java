package com.JiuShuFang.goods.service.impl;

import com.JiuShuFang.goods.constants.RedisConstant;
import com.JiuShuFang.goods.domain.dto.BookHistoryDto;
import com.JiuShuFang.goods.domain.pojo.Goods;
import com.JiuShuFang.goods.domain.query.PageQuery;
import com.JiuShuFang.goods.domain.vo.HistoryGoodsVO;
import com.JiuShuFang.goods.exception.UnauthorizedException;
import com.JiuShuFang.goods.service.IGoodsService;
import com.JiuShuFang.goods.service.IHistoryService;
import com.JiuShuFang.common.util.BeanUtils;
import com.JiuShuFang.common.util.PageDTO;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 浏览历史服务实现类
 * </p>
 *
 * @since 2026-04-11
 */
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements IHistoryService {

    private final StringRedisTemplate redisTemplate;
    private final IGoodsService goodsService;

    @Override
    public Result addHistory(BookHistoryDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = RedisConstant.BOOK_HISTORY_KEY +  dateTime + userId;
        String bookId = dto.getBookId().toString();
        long score = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(key, bookId, score);

        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        return Result.success();
    }


    @Override
    public Result<PageDTO<HistoryGoodsVO>> getHistoryPage(PageQuery query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = RedisConstant.BOOK_HISTORY_KEY + dateTime + userId;

        Long total = redisTemplate.opsForZSet().size(key);
        if (total == null || total == 0) {
            return Result.success(PageDTO.empty(0L, 0L));
        }
        int pageNum = query.getPageNo();
        int pageSize = query.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;

        Set<String> bookIdsWithScores = redisTemplate.opsForZSet()
                .reverseRange(key, offset, offset + pageSize);

        if (bookIdsWithScores == null || bookIdsWithScores.isEmpty()) {
            return Result.success(PageDTO.empty(0L, 0L));
        }

        List<HistoryGoodsVO> historyList = new ArrayList<>();
        for (String bookId : bookIdsWithScores) {
            Long goodsId = Long.parseLong(bookId);
            Goods goods = goodsService.getById(goodsId);
            if (goods != null && goods.getIsDeleted() == 0) {
                HistoryGoodsVO historyGoodsVO = BeanUtils.copyBean(goods, HistoryGoodsVO.class);
                historyList.add(historyGoodsVO);
            }
        }

        Long pages = total / pageSize + 1;

        PageDTO<HistoryGoodsVO> pageDTO = new PageDTO<>(total, pages, historyList);
        return Result.success(pageDTO);
    }
}
