package com.JiuShuFang.goods.schedule;

import com.JiuShuFang.goods.constants.RedisConstant;
import com.JiuShuFang.goods.domain.pojo.Collect;
import com.JiuShuFang.goods.service.ICollectService;
import com.JiuShuFang.common.util.CollUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CollectSchedule {
    private final ICollectService collectService;
    private final StringRedisTemplate redisTemplate;


    /**
     * 定时任务，将Redis中的收藏数据同步到数据库中
     */
    @Transactional(rollbackFor = Exception.class)
//    @Scheduled(cron = "0 0 0 1 * ?")
    public void syncCollectToDatabase() {
        String pattern = RedisConstant.COLLECT_USER_KEY + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollUtils.isEmpty(keys)) {
            return;
        }
        collectService.remove(new LambdaQueryWrapper<>());
        for (String key : keys) {
            Set<String> goodsIdSet = redisTemplate.opsForSet().members(key);
            if (CollUtils.isEmpty(goodsIdSet)) {
                continue;
            }
            String userIdStr = key.replace(RedisConstant.COLLECT_USER_KEY, "");
            Long userId = Long.parseLong(userIdStr);
            List<Collect> collectList = goodsIdSet.stream()
                    .map(goodsIdStr -> {
                        Collect collect = new Collect();
                        collect.setUserId(userId);
                        collect.setGoodsId(Long.parseLong(goodsIdStr));
                        return collect;
                    })
                    .collect(Collectors.toList());
            collectService.saveBatch(collectList);
        }
    }
}
