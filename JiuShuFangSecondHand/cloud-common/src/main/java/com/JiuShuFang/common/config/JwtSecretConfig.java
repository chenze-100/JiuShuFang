package com.JiuShuFang.common.config;

import com.JiuShuFang.common.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 启动时把各服务 application.yaml 里的 jwt.secret 注入到 JwtUtil，
 * 保证「登录签发 token」和「网关/服务验签 token」用的是同一把密钥。
 *
 * 由 @ComponentScan(basePackages = "com.JiuShuFang.common") 的服务自动装配。
 */
@Component
public class JwtSecretConfig {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            JwtUtil.setSecret(jwtSecret);
        }
    }
}
