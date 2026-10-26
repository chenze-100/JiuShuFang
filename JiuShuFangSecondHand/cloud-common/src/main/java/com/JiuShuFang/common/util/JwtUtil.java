package com.JiuShuFang.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // 与单体项目保持一致，保证旧 token 与新服务互认
    // 注意：实际密钥由各服务的 jwt.secret 配置注入（见 JwtSecretConfig），
    // 此默认值仅在未配置 jwt.secret 时兜底，生产必须用环境变量覆盖
    private static String SECRET = "mySecretKey123456789012345678901234";
    private static final long EXPIRE_MS = 24L * 60 * 60 * 1000;

    /**
     * 由 JwtSecretConfig 在启动时注入各服务配置的 jwt.secret，
     * 保证网关与所有业务服务使用同一密钥签名/验签
     */
    public static void setSecret(String secret) {
        SECRET = secret;
    }

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token，userId 放入 "userId" claim
     */
    public static String generate(Long userId, String... extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        if (extraClaims != null) {
            for (int i = 0; i + 1 < extraClaims.length; i += 2) {
                claims.put(extraClaims[i], extraClaims[i + 1]);
            }
        }
        return createToken(claims, String.valueOf(userId));
    }

    /**
     * 兼容单体 API：claims 自定义，username 作为 subject
     */
    public static String createToken(Map<String, Object> claims, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRE_MS))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 兼容单体 API，等价于 parse
     */
    public static Map<String, Object> getClaims(String token) {
        return parse(token);
    }

    public static String getUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * 兼容 "userId"（微服务新增）与 "id"（单体遗留）两种 claim 键
     */
    public static Long getUserId(String token) {
        Claims claims = parse(token);
        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.get("id");
        }
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    public static boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isExpired(String token) {
        try {
            Claims claims = parse(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
