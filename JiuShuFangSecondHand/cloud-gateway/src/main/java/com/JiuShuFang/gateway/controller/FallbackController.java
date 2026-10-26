package com.JiuShuFang.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 熔断降级端点：下游服务跳闸时由 CircuitBreaker 过滤器 forward 到这里
 */
@RestController
public class FallbackController {

    /**
     * 交易服务降级响应
     */
    @RequestMapping("/fallback/trade")
    public Mono<ResponseEntity<Map<String, Object>>> tradeFallback() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 503);
        body.put("msg", "交易服务暂时繁忙，请稍后再试");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
