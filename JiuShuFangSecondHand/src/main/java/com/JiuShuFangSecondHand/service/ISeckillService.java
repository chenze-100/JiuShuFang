package com.JiuShuFangSecondHand.service;

import com.JiuShuFangSecondHand.domain.dto.SeckillOrderMessage;
import com.JiuShuFangSecondHand.domain.pojo.SeckillBook;
import com.JiuShuFangSecondHand.domain.pojo.SeckillOrder;
import com.JiuShuFangSecondHand.domain.vo.SeckillBookVO;
import com.JiuShuFangSecondHand.domain.vo.SeckillResultVO;
import com.JiuShuFangSecondHand.util.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 秒杀 服务类
 * </p>
 *
 * @since 2026-10-14
 */
public interface ISeckillService extends IService<SeckillBook> {

    /**
     * 秒杀书籍列表
     */
    Result<List<SeckillBook>> listSeckill();

    /**
     * 秒杀详情（含 Redis 实时剩余库存与当前用户参与状态）
     */
    Result<SeckillBookVO> detail(Long seckillId);

    /**
     * 抢购：Redis Lua 原子预减 + MQ 异步落库，同步返回"排队中"
     */
    Result<String> doSeckill(Long seckillId);

    /**
     * 查询抢购结果（前端轮询）
     */
    Result<SeckillResultVO> queryResult(Long seckillId);

    /**
     * 我的秒杀订单
     */
    Result<List<SeckillOrder>> myOrders();

    /**
     * 消费落库（事务边界在此方法，由监听器调用）
     */
    void handleSeckillOrder(SeckillOrderMessage msg);

    /**
     * 失败补偿：Redis 侧回补库存、移除抢购标记、在途减一
     */
    void compensate(SeckillOrderMessage msg);

    /**
     * 对账 + 自动预热（由定时任务调用）
     */
    void reconcile();
}
