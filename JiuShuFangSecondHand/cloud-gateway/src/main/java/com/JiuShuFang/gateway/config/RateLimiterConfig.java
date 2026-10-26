package com.JiuShuFang.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 网关限流的 key 维度定义。
 * <p>
 * RequestRateLimiter 是令牌桶算法，key 决定"按谁计数"：
 * 不同 key 各自一个桶，互不影响。
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 按客户端 IP 限流，用于未登录接口（登录、注册、发送验证码）。
     * @Primary：Gateway 自动装配注入 KeyResolver 时存在多个 Bean，
     * 需要指定默认 Bean；路由里通过 #{@xxxKeyResolver} 显式指定不受影响。
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(address -> address.getAddress().getHostAddress())
                        .orElse("unknown"));
    }

    /**
     * 按用户 ID 限流，用于已登录接口。
     * JwtAuthGlobalFilter 的 order 为 -100，先于路由过滤器执行，
     * 因此这里一定读得到它注入的 X-User-Id 请求头。
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                        exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .defaultIfEmpty("anonymous");
    }
}
