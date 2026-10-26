package com.JiuShuFang.trade.controller;

import com.JiuShuFang.trade.domain.pojo.SeckillBook;
import com.JiuShuFang.trade.domain.pojo.SeckillOrder;
import com.JiuShuFang.trade.domain.vo.SeckillBookVO;
import com.JiuShuFang.trade.domain.vo.SeckillResultVO;
import com.JiuShuFang.common.exception.BusinessException;
import com.JiuShuFang.trade.service.ISeckillService;
import com.JiuShuFang.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 秒杀 前端控制器
 * </p>
 *
 * @since 2026-10-14
 */
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
@Tag(name = "限量书秒杀", description = "秒杀相关接口（Redis 预减 + MQ 异步下单）")
public class SeckillController {

    private final ISeckillService seckillService;

    /**
     * 秒杀书籍列表
     */
    @GetMapping("/list")
    @Operation(summary = "秒杀列表", description = "查询进行中/历史秒杀活动")
    public Result<List<SeckillBook>> listSeckill() {
        return seckillService.listSeckill();
    }

    /**
     * 秒杀详情
     */
    @GetMapping("/detail/{seckillId}")
    @Operation(summary = "秒杀详情", description = "含 Redis 实时剩余库存与当前用户参与状态")
    public Result<SeckillBookVO> detail(@PathVariable Long seckillId) {
        return seckillService.detail(seckillId);
    }

    /**
     * 抢购
     */
    @PostMapping("/order")
    @Operation(summary = "抢购", description = "Redis Lua 原子预减后投递 MQ，同步返回排队中")
    public Result<String> doSeckill(@RequestBody Map<String, Long> dto) {
        if (dto == null || dto.get("seckillId") == null) {
            throw new BusinessException("秒杀活动ID不能为空");
        }
        return seckillService.doSeckill(dto.get("seckillId"));
    }

    /**
     * 查询抢购结果
     */
    @GetMapping("/result")
    @Operation(summary = "抢购结果", description = "前端轮询抢购结果")
    public Result<SeckillResultVO> queryResult(@RequestParam Long seckillId) {
        return seckillService.queryResult(seckillId);
    }

    /**
     * 我的秒杀订单
     */
    @GetMapping("/my-orders")
    @Operation(summary = "我的秒杀订单", description = "查询当前登录用户的秒杀订单")
    public Result<List<SeckillOrder>> myOrders() {
        return seckillService.myOrders();
    }
}
