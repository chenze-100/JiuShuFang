package com.JiuShuFangSecondHand.controller;

import cn.hutool.core.lang.Assert;
import com.JiuShuFangSecondHand.constants.RedisConstant;
import com.JiuShuFangSecondHand.domain.pojo.SeckillBook;
import com.JiuShuFangSecondHand.domain.pojo.SeckillOrder;
import com.JiuShuFangSecondHand.exception.BusinessException;
import com.JiuShuFangSecondHand.mapper.SeckillBookMapper;
import com.JiuShuFangSecondHand.mapper.SeckillOrderMapper;
import com.JiuShuFangSecondHand.util.JwtUtil;
import com.JiuShuFangSecondHand.util.Result;
import com.JiuShuFangSecondHand.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 秒杀压测辅助接口 —— 仅在 seckill.loadtest.enabled=true 时注册，生产不暴露。
 * </p>
 * <p>
 * 三层对比口径：
 * A /loadtest/naive-order  —— 复刻现网 OrderServiceImpl 的"读-改-写"扣库存，无锁无原子，用于证明超卖
 * B /seckill/order         —— 正式接口（Redis+Lua+MQ），在 SeckillController 中
 * C /loadtest/direct-order —— 只做 update ... where stock>0，作为 B 的性能公平对手
 * </p>
 *
 * @since 2026-10-14
 */
@RestController
@RequestMapping("/seckill/loadtest")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "seckill.loadtest.enabled", havingValue = "true")
@Tag(name = "秒杀压测辅助", description = "仅在压测开关打开时可用")
public class SeckillLoadtestController {

    /** 压测用户 ID 起点：必须大于 2^31，否则 JWT 的 id claim 会被反序列化成 Integer，强转 Long 时抛错 */
    private static final long LOADTEST_USER_ID_BASE = 3000000000L;

    private final SeckillBookMapper seckillBookMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 复位：库存还原 + 清秒杀订单 + 清 Redis 秒杀相关 key
     */
    @PostMapping("/reset/{seckillId}")
    @Operation(summary = "压测复位", description = "还原库存、清空秒杀订单与 Redis key")
    public Result<Map<String, Object>> reset(@PathVariable Long seckillId) {
        SeckillBook book = seckillBookMapper.selectById(seckillId);
        Assert.notNull(book, () -> new BusinessException("秒杀活动不存在"));

        // 1.库存还原成初始值
        int stock = book.getInitialStock() == null ? 100 : book.getInitialStock();
        seckillBookMapper.resetStock(seckillId, stock);

        // 2.清空该活动的秒杀订单
        int deleted = seckillOrderMapper.delete(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getSeckillId, seckillId));

        // 3.清 Redis：库存、用户集合、在途、结果、限流
        redisTemplate.delete(RedisConstant.SECKILL_STOCK_KEY + seckillId);
        redisTemplate.delete(RedisConstant.SECKILL_USERS_KEY + seckillId);
        redisTemplate.delete(RedisConstant.SECKILL_PENDING_KEY + seckillId);
        deleteByPattern(RedisConstant.SECKILL_RESULT_KEY + seckillId + ":*");
        deleteByPattern(RedisConstant.SECKILL_LIMIT_KEY + seckillId + ":*");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("seckillId", seckillId);
        res.put("restoredStock", stock);
        res.put("deletedOrders", deleted);
        log.info("压测复位完成 seckillId={} stock={} deletedOrders={}", seckillId, stock, deleted);
        return Result.success(res);
    }

    /**
     * 批量签发压测 token，返回 count 行 "userId,token"（直接写入 users.csv）
     */
    @GetMapping("/tokens")
    @Operation(summary = "生成压测 token", description = "批量签发 JWT，返回 userId,token 行")
    public Result<List<String>> tokens(@RequestParam(defaultValue = "1000") int count) {
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long userId = LOADTEST_USER_ID_BASE + i;
            Map<String, Object> claims = new HashMap<>(2);
            claims.put("id", userId);
            String token = JwtUtil.createToken(claims, "loadtest-" + userId);
            lines.add(userId + "," + token);
        }
        return Result.success(lines);
    }

    /**
     * 层 A：复刻现网"读-改-写"扣库存（无锁、无原子、无 where 条件）—— 用于证明超卖
     */
    @PostMapping("/naive-order")
    @Operation(summary = "层A 非原子扣减", description = "复刻现网读后写逻辑，用于对比超卖")
    public Result<String> naiveOrder(@RequestBody Map<String, Long> dto) {
        Long seckillId = dto == null ? null : dto.get("seckillId");
        Long userId = UserContext.getUserId();
        Assert.notNull(seckillId, () -> new BusinessException("秒杀活动ID不能为空"));
        Assert.notNull(userId, () -> new BusinessException("未登录"));

        // ↓↓↓ 与 OrderServiceImpl.CreateOrderDetailAndDeductStock 同一套写法：读 -> 内存减 -> 整行覆盖
        SeckillBook book = seckillBookMapper.selectById(seckillId);
        if (book == null) {
            return Result.error("活动不存在");
        }
        Integer stock = book.getSeckillStock();
        if (stock == null || stock < 1) {
            return Result.error("库存不足");
        }
        book.setSeckillStock(stock - 1);
        seckillBookMapper.updateById(book);
        // ↑↑↑ 读后写：并发下多个线程读到同一个 stock，各自写 stock-1，超卖

        seckillOrderMapper.insert(new SeckillOrder()
                .setSeckillId(seckillId)
                .setGoodsId(book.getGoodsId())
                .setUserId(userId)
                .setBookName(book.getBookName())
                .setPrice(book.getPrice())
                .setStatus(1));
        return Result.success("下单成功");
    }

    /**
     * 层 C：数据库原子扣减（update ... where seckill_stock > 0），作为层 B 的性能公平对手
     */
    @PostMapping("/direct-order")
    @Operation(summary = "层C 原子扣减", description = "DB 原子 update where stock>0，正确但热点行竞争")
    public Result<String> directOrder(@RequestBody Map<String, Long> dto) {
        Long seckillId = dto == null ? null : dto.get("seckillId");
        Long userId = UserContext.getUserId();
        Assert.notNull(seckillId, () -> new BusinessException("秒杀活动ID不能为空"));
        Assert.notNull(userId, () -> new BusinessException("未登录"));

        int rows = seckillBookMapper.deductStock(seckillId);
        if (rows == 0) {
            return Result.error("已售罄");
        }
        SeckillBook book = seckillBookMapper.selectById(seckillId);
        seckillOrderMapper.insert(new SeckillOrder()
                .setSeckillId(seckillId)
                .setGoodsId(book.getGoodsId())
                .setUserId(userId)
                .setBookName(book.getBookName())
                .setPrice(book.getPrice())
                .setStatus(1));
        return Result.success("下单成功");
    }

    /**
     * 终局统计：DB 库存、成功单数、Redis 库存、在途
     */
    @GetMapping("/stat")
    @Operation(summary = "终局统计", description = "返回 DB 库存/成功单数/Redis 库存/在途")
    public Result<Map<String, Object>> stat(@RequestParam Long seckillId) {
        SeckillBook book = seckillBookMapper.selectById(seckillId);
        Assert.notNull(book, () -> new BusinessException("秒杀活动不存在"));
        Long successCount = seckillOrderMapper.selectCount(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getSeckillId, seckillId));
        String stockVal = redisTemplate.opsForValue().get(RedisConstant.SECKILL_STOCK_KEY + seckillId);
        String pendingVal = redisTemplate.opsForValue().get(RedisConstant.SECKILL_PENDING_KEY + seckillId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("seckillId", seckillId);
        res.put("initialStock", book.getInitialStock());
        res.put("dbStock", book.getSeckillStock());
        res.put("successCount", successCount);
        res.put("redisStock", stockVal);
        res.put("pending", pendingVal);
        return Result.success(res);
    }

    private void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
