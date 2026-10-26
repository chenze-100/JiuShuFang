package com.JiuShuFang.trade.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.JiuShuFang.trade.constants.RedisConstant;
import com.JiuShuFang.trade.domain.dto.SeckillOrderMessage;
import com.JiuShuFang.trade.domain.pojo.SeckillBook;
import com.JiuShuFang.trade.domain.pojo.SeckillOrder;
import com.JiuShuFang.trade.domain.vo.SeckillBookVO;
import com.JiuShuFang.trade.domain.vo.SeckillResultVO;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.trade.exception.UnauthorizedException;
import com.JiuShuFang.trade.mapper.SeckillBookMapper;
import com.JiuShuFang.trade.mapper.SeckillOrderMapper;
import com.JiuShuFang.trade.service.ISeckillService;
import com.JiuShuFang.common.util.BeanUtils;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.JiuShuFang.trade.constants.MqConstant.Seckill.*;

/**
 * <p>
 * 秒杀 服务实现类
 * </p>
 * <p>
 * 高并发要点：
 * 1. Redis + Lua 单脚本原子预减（去重 / 判库存 / DECR / SADD / 在途 +1 一次往返完成）
 * 2. RabbitMQ 异步削峰，HTTP 线程只碰 Redis 与 MQ，不等 DB
 * 3. 一人一单双保险：Redis Set + DB 唯一索引 uk_user_seckill
 * 4. DB 原子扣减 update ... where seckill_stock > 0 作兜底防线
 * 5. 不变式对账 redisStock + pending == dbStock
 * </p>
 *
 * @since 2026-10-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillServiceImpl extends ServiceImpl<SeckillBookMapper, SeckillBook>
        implements ISeckillService {

    /** 结果状态 */
    public static final String R_PROCESSING = "PROCESSING";
    public static final String R_SUCCESS = "SUCCESS";
    public static final String R_FAIL = "FAIL";
    public static final String R_SOLD_OUT = "SOLD_OUT";
    public static final String R_REPEAT = "REPEAT";
    public static final String R_NONE = "NONE";

    private static final long RESULT_TTL_MINUTES = 5L;

    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final DefaultRedisScript<Long> seckillDeductScript;

    /** 秒级固定窗口限流阈值（保护栏，不是压测瓶颈） */
    @Value("${seckill.limit.per-second:20000}")
    private long limitPerSecond;

    @Override
    public Result<List<SeckillBook>> listSeckill() {
        List<SeckillBook> list = lambdaQuery()
                .eq(SeckillBook::getIsDeleted, 0)
                .orderByDesc(SeckillBook::getStartTime)
                .list();
        return Result.success(list);
    }

    @Override
    public Result<SeckillBookVO> detail(Long seckillId) {
        Assert.notNull(seckillId, () -> new BusinessException("秒杀活动ID不能为空"));
        SeckillBook book = getById(seckillId);
        Assert.notNull(book, () -> new BusinessException("秒杀活动不存在"));

        SeckillBookVO vo = BeanUtils.copyBean(book, SeckillBookVO.class);
        Long userId = UserContext.getUserId();

        Integer redisStock = getRedisStock(seckillId);
        vo.setRemainStock(redisStock == null ? book.getSeckillStock() : redisStock);
        if (userId != null) {
            vo.setJoined(isJoined(seckillId, userId));
            vo.setProcessing(R_PROCESSING.equals(redisTemplate.opsForValue().get(resultKey(seckillId, userId))));
        }
        return Result.success(vo);
    }

    @Override
    public Result<String> doSeckill(Long seckillId) {
//        Long userId = UserContext.getUserId();
        Long userId = 2042121639959470081l;
        Assert.notNull(userId, () -> new UnauthorizedException("请先登录"));
        Assert.notNull(seckillId, () -> new BusinessException("秒杀活动ID不能为空"));

        // ---------- 1. 活动有效性校验 ----------
        SeckillBook book = getById(seckillId);
        Assert.notNull(book, () -> new BusinessException("秒杀活动不存在"));
        Assert.isFalse(Integer.valueOf(1).equals(book.getIsDeleted()), () -> new BusinessException("秒杀活动不存在"));
        Assert.equals(book.getStatus(), 1, () -> new BusinessException("秒杀活动已结束"));
        LocalDateTime now = LocalDateTime.now();
        Assert.isFalse(now.isBefore(book.getStartTime()), () -> new BusinessException("秒杀尚未开始"));
        Assert.isFalse(now.isAfter(book.getEndTime()), () -> new BusinessException("秒杀已结束"));

        // ---------- 2. 秒级固定窗口限流 ----------
        if (rateLimited(seckillId)) {
            return Result.error("当前参与人数过多，请稍后重试");
        }

        String stockKey = RedisConstant.SECKILL_STOCK_KEY + seckillId;
        String usersKey = RedisConstant.SECKILL_USERS_KEY + seckillId;
        String pendingKey = RedisConstant.SECKILL_PENDING_KEY + seckillId;
        String resultKey = resultKey(seckillId, userId);

        // ---------- 3. 自动预热兜底（定时任务已预热，这里再兜一层） ----------
        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, String.valueOf(book.getSeckillStock()));
        }

        // ---------- 4. Lua 原子预减：一次往返完成去重+判库存+DECR+SADD+在途+1 ----------
        Long code = redisTemplate.execute(seckillDeductScript,
                Arrays.asList(stockKey, usersKey, pendingKey), String.valueOf(userId));
        long value = code == null ? 0L : code;

        if (value == 0L) {
            setResult(resultKey, R_SOLD_OUT);
            return Result.error("已售罄");
        }
        if (value == -1L) {
            setResult(resultKey, R_REPEAT);
            return Result.error("您已参与过本次抢购");
        }

        // ---------- 5. 抢到名额：写"处理中"，投递 MQ 并同步等 broker 确认 ----------
        setResult(resultKey, R_PROCESSING);

        SeckillOrderMessage msg = new SeckillOrderMessage();
        msg.setSeckillId(seckillId);
        msg.setUserId(userId);

        Boolean confirmed;
        try {
            confirmed = rabbitTemplate.invoke(ops -> {
                ops.convertAndSend(SECKILL_DIRECT_EXCHANGE, SECKILL_ROUTING_KEY, msg);
                return ops.waitForConfirms(5000);
            });
        } catch (Exception e) {
            log.error("秒杀消息投递异常 seckillId={} userId={}", seckillId, userId, e);
            confirmed = Boolean.FALSE;
        }

        if (!Boolean.TRUE.equals(confirmed)) {
            // 消息没进 broker，Redis 扣减必须回补，否则库存白扣
            log.error("秒杀消息未被 broker 确认，执行回补 seckillId={} userId={}", seckillId, userId);
            compensate(msg);
            setResult(resultKey, R_FAIL);
            return Result.error("系统繁忙，请稍后重试");
        }
        return Result.success("排队中，请稍候查看结果");
    }

    @Override
    public Result<SeckillResultVO> queryResult(Long seckillId) {
        Long userId = UserContext.getUserId();
        Assert.notNull(userId, () -> new UnauthorizedException("请先登录"));
        Assert.notNull(seckillId, () -> new BusinessException("秒杀活动ID不能为空"));

        String status = redisTemplate.opsForValue().get(resultKey(seckillId, userId));
        if (StrUtil.isBlank(status)) {
            // Redis 无结果（过期/被清）时以 DB 为准，避免误报
            SeckillOrder order = seckillOrderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                    .eq(SeckillOrder::getSeckillId, seckillId)
                    .eq(SeckillOrder::getUserId, userId));
            return Result.success(buildResult(order == null ? R_NONE : R_SUCCESS,
                    order == null ? null : order.getId()));
        }
        Long orderId = null;
        if (R_SUCCESS.equals(status)) {
            SeckillOrder order = seckillOrderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                    .eq(SeckillOrder::getSeckillId, seckillId)
                    .eq(SeckillOrder::getUserId, userId));
            orderId = order == null ? null : order.getId();
        }
        return Result.success(buildResult(status, orderId));
    }

    @Override
    public Result<List<SeckillOrder>> myOrders() {
        Long userId = UserContext.getUserId();
        Assert.notNull(userId, () -> new UnauthorizedException("请先登录"));
        List<SeckillOrder> list = seckillOrderMapper.selectList(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getUserId, userId)
                .orderByDesc(SeckillOrder::getCreateTime));
        return Result.success(list);
    }

    /**
     * 消费落库：事务边界在此方法。
     * 监听器负责 catch 异常并调用 compensate，绝不能把异常吞在本方法内部，
     * 否则异常不会穿过 Spring 代理，事务不会回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void handleSeckillOrder(SeckillOrderMessage msg) {
        Long seckillId = msg.getSeckillId();
        Long userId = msg.getUserId();

        SeckillBook book = getById(seckillId);
        if (book == null) {
            log.error("秒杀活动不存在，跳过落库 seckillId={}", seckillId);
            return;
        }

        // ---------- 1. 幂等预检：重复投递直接返回（Lua SADD 已保证正常链路不会出现） ----------
        Long exists = seckillOrderMapper.selectCount(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getSeckillId, seckillId)
                .eq(SeckillOrder::getUserId, userId));
        if (exists != null && exists > 0) {
            log.warn("秒杀订单重复投递，幂等跳过 seckillId={} userId={}", seckillId, userId);
            return;
        }

        // ---------- 2. 插入秒杀订单（uk_user_seckill 唯一索引是最终保险） ----------
        SeckillOrder order = new SeckillOrder()
                .setSeckillId(seckillId)
                .setGoodsId(book.getGoodsId())
                .setUserId(userId)
                .setBookName(book.getBookName())
                .setPrice(book.getPrice())
                .setStatus(1);
        try {
            seckillOrderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发重复：属于幂等场景，同样不补偿（首次已结算），直接返回
            log.warn("秒杀订单唯一索引冲突，幂等跳过 seckillId={} userId={}", seckillId, userId);
            return;
        }

        // ---------- 3. DB 原子扣减：唯一索引之外的第二道正确性防线 ----------
        int rows = baseMapper.deductStock(seckillId);
        if (rows == 0) {
            // 抛出去触发事务回滚（订单插入一并回滚），Redis 侧由监听器 compensate
            throw new BusinessException("秒杀库存不足");
        }

        // ---------- 4. 结算在途 + 写成功结果（方法末尾，事务已无失败点） ----------
        redisTemplate.opsForValue().decrement(RedisConstant.SECKILL_PENDING_KEY + seckillId);
        setResult(resultKey(seckillId, userId), R_SUCCESS);
    }

    @Override
    public void compensate(SeckillOrderMessage msg) {
        if (msg == null || msg.getSeckillId() == null || msg.getUserId() == null) {
            return;
        }
        Long seckillId = msg.getSeckillId();
        String userId = String.valueOf(msg.getUserId());
        try {
            // 库存加回
            redisTemplate.opsForValue().increment(RedisConstant.SECKILL_STOCK_KEY + seckillId);
            // 移除抢购标记，允许用户重试
            redisTemplate.opsForSet().remove(RedisConstant.SECKILL_USERS_KEY + seckillId, userId);
            // 在途减一
            redisTemplate.opsForValue().decrement(RedisConstant.SECKILL_PENDING_KEY + seckillId);
        } catch (Exception e) {
            log.error("秒杀补偿失败 seckillId={} userId={}", seckillId, msg.getUserId(), e);
        }
    }

    @Override
    public void reconcile() {
        List<SeckillBook> books = lambdaQuery()
                .eq(SeckillBook::getIsDeleted, 0)
                .eq(SeckillBook::getStatus, 1)
                .list();
        if (books.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (SeckillBook book : books) {
            Long id = book.getId();
            String stockKey = RedisConstant.SECKILL_STOCK_KEY + id;

            // ---------- 自动预热：库存 key 不存在则用 DB 值初始化 ----------
            if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
                if (now.isAfter(book.getEndTime())) {
                    continue;
                }
                redisTemplate.opsForValue().set(stockKey, String.valueOf(book.getSeckillStock()));
                log.info("秒杀自动预热 seckillId={} stock={}", id, book.getSeckillStock());
                continue;
            }

            // ---------- 对账：不变式 redisStock + pending == dbStock ----------
            String stockVal = redisTemplate.opsForValue().get(stockKey);
            String pendingVal = redisTemplate.opsForValue().get(RedisConstant.SECKILL_PENDING_KEY + id);
            int redisStock = StrUtil.isBlank(stockVal) ? 0 : Integer.parseInt(stockVal);
            int pending = StrUtil.isBlank(pendingVal) ? 0 : Integer.parseInt(pendingVal);
            Integer dbStock = book.getSeckillStock();
            if (dbStock == null) {
                continue;
            }

            long dbSuccessCount = seckillOrderMapper.selectCount(new LambdaQueryWrapper<SeckillOrder>()
                    .eq(SeckillOrder::getSeckillId, id));

            if (redisStock + pending != dbStock) {
                int fixStock = dbStock - pending;
                log.error("秒杀库存不一致 seckillId={} redisStock={} pending={} dbStock={} " +
                                "落库单数={} 初始库存={}，以 DB 为准回写 redisStock={}",
                        id, redisStock, pending, dbStock, dbSuccessCount, book.getInitialStock(), fixStock);
                redisTemplate.opsForValue().set(stockKey, String.valueOf(fixStock));
            }
        }
    }

    // ==================== 私有工具方法 ====================

    private boolean rateLimited(Long seckillId) {
        long window = System.currentTimeMillis() / 1000;
        String key = RedisConstant.SECKILL_LIMIT_KEY + seckillId + ":" + window;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, 2, TimeUnit.SECONDS);
        }
        return count != null && count > limitPerSecond;
    }

    private Boolean isJoined(Long seckillId, Long userId) {
        Boolean member = redisTemplate.opsForSet()
                .isMember(RedisConstant.SECKILL_USERS_KEY + seckillId, String.valueOf(userId));
        if (Boolean.TRUE.equals(member)) {
            return true;
        }
        // Redis 被清时以 DB 为准
        Long exists = seckillOrderMapper.selectCount(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getSeckillId, seckillId)
                .eq(SeckillOrder::getUserId, userId));
        return exists != null && exists > 0;
    }

    private Integer getRedisStock(Long seckillId) {
        String val = redisTemplate.opsForValue().get(RedisConstant.SECKILL_STOCK_KEY + seckillId);
        return StrUtil.isBlank(val) ? null : Integer.valueOf(val);
    }

    private String resultKey(Long seckillId, Long userId) {
        return RedisConstant.SECKILL_RESULT_KEY + seckillId + ":" + userId;
    }

    private void setResult(String resultKey, String status) {
        redisTemplate.opsForValue().set(resultKey, status, RESULT_TTL_MINUTES, TimeUnit.MINUTES);
    }

    private SeckillResultVO buildResult(String status, Long orderId) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setStatus(status);
        vo.setSeckillOrderId(orderId);
        switch (status) {
            case R_PROCESSING -> vo.setMessage("排队中，请稍候…");
            case R_SUCCESS -> vo.setMessage("抢购成功！");
            case R_FAIL -> vo.setMessage("系统繁忙，抢购失败，请重试");
            case R_SOLD_OUT -> vo.setMessage("已售罄");
            case R_REPEAT -> vo.setMessage("您已参与过本次抢购");
            default -> vo.setMessage("尚未参与");
        }
        return vo;
    }
}
