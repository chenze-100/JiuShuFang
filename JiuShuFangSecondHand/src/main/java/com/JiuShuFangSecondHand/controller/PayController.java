package com.JiuShuFangSecondHand.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.JiuShuFangSecondHand.service.IPayService;
import com.JiuShuFangSecondHand.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;



@Tag(name = "支付", description = "支付相关接口")
@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {



    private final IPayService payService;

    /**
     * 电脑网站支付接口
     */
    @GetMapping("/pc")
    public void pcPay(HttpServletResponse response,
                      Long orderId) {
        payService.pcPay(response, orderId);
    }

    @Operation(summary = "创建pcPay")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        return payService.notify(request);
    }

    /**
     * 测试阶段模拟支付接口：点击支付直接成功，完成订单/支付单/流水的落库，
     * 不对接真实支付渠道。前端传 orderId + amount
     */
    @Operation(summary = "测试阶段模拟支付")
    @PostMapping("/balance")
    public Result<String> balancePay(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(String.valueOf(body.get("orderId")));
        Object amountObj = body.get("amount");
        BigDecimal amount = amountObj != null
                ? new BigDecimal(String.valueOf(amountObj))
                : null;
        log.info("测试支付请求 orderId:{}, amount:{}", orderId, amount);
        payService.balancePay(orderId, amount);
        return Result.success("支付成功");
    }

}
