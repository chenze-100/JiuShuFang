package com.JiuShuFang.trade.schedule;

import com.JiuShuFang.trade.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 秒杀对账任务：
 * 1. 自动预热（Redis 库存 key 不存在时用 DB 值初始化）
 * 2. 不变式对账 redisStock + pending == dbStock，不一致以 DB 为准回写并告警
 * </p>
 *
 * @since 2026-10-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeckillReconcileSchedule {

    private final ISeckillService seckillService;

    @Scheduled(fixedDelay = 60000)
    public void reconcile() {
        try {
            seckillService.reconcile();
        } catch (Exception e) {
            log.error("秒杀对账任务执行异常", e);
        }
    }
}
