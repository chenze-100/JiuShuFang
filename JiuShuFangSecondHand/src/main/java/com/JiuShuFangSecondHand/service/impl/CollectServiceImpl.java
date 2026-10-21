package com.JiuShuFangSecondHand.service.impl;

import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.domain.dto.CollectDto;
import com.JiuShuFangSecondHand.domain.vo.GoodsVO;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.exception.UnauthorizedException;
import com.JiuShuFangSecondHand.service.ICollectService;
import com.JiuShuFangSecondHand.service.IGoodsService;
import com.JiuShuFangSecondHand.util.BeanUtils;
import com.JiuShuFangSecondHand.util.CollUtils;
import com.JiuShuFangSecondHand.util.UserContext;
import com.JiuShuFangSecondHand.domain.pojo.Collect;
import com.JiuShuFangSecondHand.domain.pojo.Goods;
import com.JiuShuFangSecondHand.mapper.CollectMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.JiuShuFangSecondHand.constants.MessageConstant.MAX_COLLECT;

/**
 * <p>
 * 用户收藏表 服务实现类
 * </p>
 *
 * @since 2026-04-09
 */
@Service
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements ICollectService {


    private final IGoodsService goodsService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addCollect(CollectDto dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        String key = RedisConstant.COLLECT_USER_KEY + userId;

        if (!dto.getIsCollect()) {
            redisTemplate.opsForSet().remove(key, String.valueOf(dto.getGoodsId()));
            return;
        }
        Long size = redisTemplate.opsForSet().size(key);
        if (size != null && size >= MAX_COLLECT) {
            throw new BusinessException("收藏数量达到上限");
        }
        redisTemplate.opsForSet().add(key, String.valueOf(dto.getGoodsId()));
    }

    @Override
    public List<GoodsVO> getCollectList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        String key = RedisConstant.COLLECT_USER_KEY + userId;
        Set<String> goodsIdSet = redisTemplate.opsForSet().members(key);

        if (CollUtils.isEmpty(goodsIdSet)) {
            return List.of();
        }

        Set<Long> goodsIds = goodsIdSet.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
        List<Goods> goodsList = goodsService.listByIds(goodsIds);
        return goodsList.stream().map(goods -> {
                    GoodsVO goodsVO = BeanUtils.copyProperties(goods, GoodsVO.class);
                    goodsVO.setIsLike(true);
                    return goodsVO;
                }).toList();
    }


}
