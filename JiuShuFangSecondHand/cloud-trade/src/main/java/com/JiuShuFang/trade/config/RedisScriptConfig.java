package com.JiuShuFang.trade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Redis Lua 脚本配置
 */
@Configuration
public class RedisScriptConfig {

    /**
     * 秒杀库存预减脚本：一次 Redis 往返完成 去重 + 判库存 + 扣减 + 记录在途
     */
    @Bean
    public DefaultRedisScript<Long> seckillDeductScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/seckill_deduct.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
